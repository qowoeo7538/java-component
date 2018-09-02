package org.shaw.task;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.shaw.core.Constants;
import org.shaw.task.support.AbortPolicyWithReport;
import org.shaw.task.support.AfterFunction;
import org.shaw.task.support.BeforeFunction;
import org.shaw.task.support.DefaultFuture;
import org.shaw.task.support.ExecutorCompletionService;
import org.shaw.task.support.TaskExecutionException;
import org.shaw.task.support.ThrottleSupport;
import org.springframework.util.Assert;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureTask;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * StandardThreadExecutor
 * 执行策略：	优先扩充线程到maxThread，再offer到queue，如果满了就reject
 * 适应场景： 适用于低于CPU运行效率的业务
 * <p>
 * ThreadPoolTaskExecutor
 * 执行策略：	优先将任务offer到queue，再扩充线程到maxThread，如果满了就reject
 * 适应场景： 高效率任务
 * <p>
 * 建议线程数目 = （（线程等待时间+线程处理时间）/线程处理时间 ）* CPU数目
 */
public class ThreadPoolTaskExecutor extends AbstractExecutorService {

    /**
     * 核心线程池大小
     * <p>
     * 当线程池小于corePoolSize时，新提交任务将创建一个新线程执行任务，
     * 即使此时线程池中存在空闲线程。
     * <p>
     * 当线程池达到corePoolSize时，新提交任务将被放入任务队列中.
     * <p>
     * 默认CPU密集型应用数量
     *
     * @see org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor#corePoolSize
     */
    private static final int DEFAULT_CORE_POOL_SIZE = Constants.CORE_SIZE + 1;

    /**
     * 最大线程池大小
     * <p>
     * 如果任务队列已满,将创建最大线程池的数量执行任务,如果超出最大线程池的大小,
     * 将提交给RejectedExecutionHandler处理
     * <p>
     * 默认IO密集型应用数量
     *
     * @see org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor#maxPoolSize
     */
    private static final int DEFAULT_MAX_POOL_SIZE = 2 * Constants.CORE_SIZE + 1;

    /**
     * 线程池中超过核心线程数目的空闲线程最大存活时间；
     * 可以allowCoreThreadTimeOut(true)使得核心线程有效时间
     */
    private static final int DEFAULT_KEEP_ALIVE_SECONDS = 60;

    /**
     * 线程池限流对象
     */
    private final ConcurrencyThrottleAdapter throttleSupport;

    private final ThreadPoolExecutor threadPoolExecutor;

    //======================================

    /**
     * 线程池超时关闭时间
     */
    private int awaitTerminationSeconds = 0;

    private BeforeFunction before = (r, t) -> {
    };

    private AfterFunction after = (r, t) -> {
    };

    public ThreadPoolTaskExecutor() {
        this(DEFAULT_CORE_POOL_SIZE, DEFAULT_MAX_POOL_SIZE);
    }

    public ThreadPoolTaskExecutor(final int corePoolSize, final int maxPoolSize) {
        this(corePoolSize, maxPoolSize, DEFAULT_KEEP_ALIVE_SECONDS);
    }

    public ThreadPoolTaskExecutor(final int corePoolSize, final int maxPoolSize, final int keepAliveSeconds) {
        this(corePoolSize, maxPoolSize, keepAliveSeconds, maxPoolSize);
    }

    public ThreadPoolTaskExecutor(final int corePoolSize, final int maxPoolSize, final int keepAliveSeconds, final int queueCapacity) {
        this(corePoolSize, maxPoolSize, keepAliveSeconds, queueCapacity, Executors.defaultThreadFactory());
    }

    public ThreadPoolTaskExecutor(final int corePoolSize, final int maxPoolSize, final int keepAliveSeconds, final int queueCapacity, final ThreadFactory threadFactory) {
        this(corePoolSize, maxPoolSize, keepAliveSeconds, queueCapacity, threadFactory, new AbortPolicyWithReport());
    }

    public ThreadPoolTaskExecutor(final int corePoolSize, final int maxPoolSize, final int keepAliveSeconds, final int queueCapacity, final ThreadFactory threadFactory, final RejectedExecutionHandler rejectedExecutionHandler) {
        this.threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize > corePoolSize ? maxPoolSize : corePoolSize,
                keepAliveSeconds, TimeUnit.SECONDS, new ExecutorQueue(), threadFactory, rejectedExecutionHandler) {
            @Override
            protected void beforeExecute(final Thread t, final Runnable r) {
                before.beforeExecute(r, t);
            }

            @Override
            protected void afterExecute(final Runnable r, final Throwable t) {
                after.afterExecute(r, t);
            }
        };

        this.throttleSupport = new ConcurrencyThrottleAdapter();
        // 设置最大并发数 maxPoolSize + queueCapacity
        this.throttleSupport.setConcurrencyLimit(maxPoolSize + queueCapacity);
    }

    public ThreadPoolExecutor getThreadPoolExecutor() throws IllegalStateException {
        Assert.state(this.threadPoolExecutor != null, "ThreadPoolExecutor 没有初始化!");
        return this.threadPoolExecutor;
    }

    /**
     * @return 运行任务数量
     */
    public int getTaskCount() {
        return this.throttleSupport.getConcurrencyCount().get();
    }

    public void setBefore(final BeforeFunction before) {
        this.before = before;
    }

    public void setAfter(final AfterFunction after) {
        this.after = after;
    }

    @Override
    public void execute(final Runnable task) {
        this.throttleSupport.beforeAccess(task);
        this.threadPoolExecutor.execute(new ConcurrencyThrottlingRunnable(task));
    }

    @Override
    public <T> Future<T> submit(final Callable<T> task) {
        this.throttleSupport.beforeAccess(task);
        return this.threadPoolExecutor.submit(new ConcurrencyThrottlingCallable<>(task));
    }

    /**
     * @param task         任务
     * @param defaultValue 默认返回值
     * @return 如果超出并发设置，则使用默认返回值快速响应
     */
    public <T> Future<T> submit(final Callable<T> task, final T defaultValue) {
        if (this.throttleSupport.isLimit()) {
            return new DefaultFuture<>(defaultValue);
        }
        return this.threadPoolExecutor.submit(new ConcurrencyThrottlingCallable<>(task));
    }

    @Override
    public Future<?> submit(final Runnable task) {
        this.throttleSupport.beforeAccess(task);
        return this.threadPoolExecutor.submit(new ConcurrencyThrottlingRunnable(task));
    }

    @Override
    public <T> Future<T> submit(final Runnable task, final T result) {
        this.throttleSupport.beforeAccess(task);
        return this.threadPoolExecutor.submit(new ConcurrencyThrottlingRunnable(task), result);
    }

    public ListenableFuture<?> submitListenable(final Runnable task) {
        final ListenableFutureTask<Object> future = new ListenableFutureTask<>(new ConcurrencyThrottlingRunnable(task), null);
        this.throttleSupport.beforeAccess(future);
        this.threadPoolExecutor.execute(future);
        return future;
    }

    public <T> ListenableFuture<T> submitListenable(final Callable<T> task) {
        final ListenableFutureTask<T> future = new ListenableFutureTask<>(new ConcurrencyThrottlingCallable<>(task));
        this.throttleSupport.beforeAccess(future);
        this.threadPoolExecutor.execute(future);
        return future;
    }

    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
        final MutableList<Future<T>> futures = new FastList<>();
        boolean done = false;
        try {
            for (Iterator<? extends Callable<T>> iterator = tasks.iterator(); iterator.hasNext(); ) {
                Callable<T> callable = iterator.next();
                this.throttleSupport.beforeAccess(callable);
                futures.add(this.submit(callable));
            }
            for (int i = 0, size = futures.size(); i < size; i++) {
                Future<T> f = futures.get(i);
                if (!f.isDone()) {
                    try {
                        f.get();
                    } catch (CancellationException ignore) {
                    } catch (ExecutionException ignore) {
                    }
                }
            }
            done = true;
        } finally {
            if (!done) {
                for (int i = 0, size = futures.size(); i < size; i++) {
                    futures.get(i).cancel(true);
                }
            }
        }
        return futures;
    }

    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks,
                                         final long timeout, final TimeUnit unit)
            throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final MutableList<Future<T>> futures = new FastList<>();
        for (Callable<T> callable : tasks) {
            futures.add(new FutureTask<>(callable));
        }
        final long deadline = System.nanoTime() + nanos;
        final int size = futures.size();
        boolean done = false;
        try {
            for (int i = 0; i < size; i++) {
                this.execute((Runnable) futures.get(i));
                nanos = deadline - System.nanoTime();
                if (nanos <= 0L) {
                    return futures;
                }
            }

            for (int i = 0; i < size; i++) {
                Future<T> f = futures.get(i);
                if (!f.isDone()) {
                    if (nanos <= 0L) {
                        return futures;
                    }
                    try {
                        f.get(nanos, TimeUnit.NANOSECONDS);
                    } catch (CancellationException ignore) {
                    } catch (ExecutionException ignore) {
                    } catch (TimeoutException toe) {
                        return futures;
                    }
                    nanos = deadline - System.nanoTime();
                }
            }
            done = true;
        } finally {
            if (!done) {
                for (int i = 0; i < size; i++) {
                    futures.get(i).cancel(true);
                }
            }
        }
        return futures;
    }

    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        try {
            return doInvokeAny(tasks, false, 0);
        } catch (TimeoutException cannotHappen) {
            return null;
        }
    }

    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return doInvokeAny(tasks, true, unit.toNanos(timeout));
    }

    @Override
    public void shutdown() {
        this.threadPoolExecutor.shutdown();
        awaitTerminationIfNecessary(this.threadPoolExecutor);
    }

    @Override
    public List<Runnable> shutdownNow() {
        List<Runnable> list = this.threadPoolExecutor.shutdownNow();
        for (Runnable remainingTask : list) {
            cancelRemainingTask(remainingTask);
        }
        awaitTerminationIfNecessary(this.threadPoolExecutor);
        return list;
    }

    @Override
    public boolean isShutdown() {
        return this.threadPoolExecutor.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return this.threadPoolExecutor.isTerminated();
    }

    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        return this.threadPoolExecutor.awaitTermination(timeout, unit);
    }

    private <T> T doInvokeAny(Collection<? extends Callable<T>> tasks,
                              boolean timed, long nanos)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (tasks == null) {
            throw new NullPointerException();
        }
        int ntasks = tasks.size();
        if (ntasks == 0) {
            throw new IllegalArgumentException();
        }
        MutableList<Future<T>> futures = new FastList<>(ntasks);
        ExecutorCompletionService<T> ecs = new ExecutorCompletionService<>(this, throttleSupport);

        try {
            ExecutionException ee = null;
            final long deadline = timed ? System.nanoTime() + nanos : 0L;
            Iterator<? extends Callable<T>> it = tasks.iterator();

            futures.add(ecs.submit(it.next()));
            --ntasks;
            int active = 1;

            for (; ; ) {
                Future<T> f = ecs.poll();
                if (f == null) {
                    if (ntasks > 0) {
                        --ntasks;
                        futures.add(ecs.submit(it.next()));
                        ++active;
                    } else if (active == 0) {
                        break;
                    } else if (timed) {
                        f = ecs.poll(nanos, TimeUnit.NANOSECONDS);
                        if (f == null) {
                            throw new TimeoutException();
                        }

                        nanos = deadline - System.nanoTime();
                    } else {
                        f = ecs.take();
                    }
                }
                if (f != null) {
                    --active;
                    try {
                        return f.get();
                    } catch (ExecutionException eex) {
                        ee = eex;
                    } catch (RuntimeException rex) {
                        ee = new ExecutionException(rex);
                    }
                }
            }
            if (ee == null) {
                ee = new TaskExecutionException();
            }
            throw ee;
        } finally {
            for (int i = 0, size = futures.size(); i < size; i++) {
                futures.get(i).cancel(true);
            }
        }
    }

    /**
     * 尝试取消剩下的 {@code Future} 任务
     *
     * @param task 任务
     */
    private void cancelRemainingTask(final Runnable task) {
        if (task instanceof Future) {
            // 尝试取消任务，如果任务已经启动，通过 mayInterruptIfRunning 参数来终止该任务。
            ((Future<?>) task).cancel(true);
        }
    }

    private void awaitTerminationIfNecessary(final ExecutorService executor) {
        if (this.awaitTerminationSeconds > 0) {
            try {
                if (!executor.awaitTermination(this.awaitTerminationSeconds, TimeUnit.SECONDS)) {
                    // TODO 日志记录
                }
            } catch (final InterruptedException ex) {
                // TODO 日志记录
                Thread.currentThread().interrupt();
            }
        }
    }

    private class ConcurrencyThrottleAdapter extends ThrottleSupport {

        public ConcurrencyThrottleAdapter() {
            super(getThreadPoolExecutor());
        }

        @Override
        protected void beforeAccess(final Runnable task) {
            super.beforeAccess(task);
        }

        @Override
        protected <V> void beforeAccess(final Callable<V> task) {
            super.beforeAccess(task);
        }

        @Override
        protected void afterAccess() {
            super.afterAccess();
        }
    }

    private class ConcurrencyThrottlingRunnable implements Runnable {

        private final Runnable target;

        public ConcurrencyThrottlingRunnable(final Runnable target) {
            this.target = target;
        }

        @Override
        public void run() {
            try {
                this.target.run();
            } catch (final RejectedExecutionException rx) {
                // 尝试放入队列，失败则直接使用失败策略
                if (!((ExecutorQueue) getThreadPoolExecutor().getQueue()).force(target)) {
                    getThreadPoolExecutor().getRejectedExecutionHandler().rejectedExecution(target, getThreadPoolExecutor());
                }
            } finally {
                throttleSupport.afterAccess();
            }
        }
    }

    private class ConcurrencyThrottlingCallable<V> implements Callable<V> {

        private final Callable<V> target;

        public ConcurrencyThrottlingCallable(final Callable<V> target) {
            this.target = target;
        }

        @Override
        public V call() throws Exception {
            try {
                return target.call();
            } finally {
                throttleSupport.afterAccess();
            }
        }
    }

    /**
     * TODO 尝试用 RingBuffer 改造
     * <p>
     * {@code BlockingQueue} 存取锁，会导致性能低下，
     * 通过 {@code LinkedTransferQueue} 预占模式，保证更好的性能,
     * 但是生产者速度如果过快，会导致内存溢出
     */
    private class ExecutorQueue extends LinkedTransferQueue<Runnable> implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 将任务放入队列
         *
         * @param task {@code Runnable}
         * @return {@code boolean} 是否放入成功
         */
        public boolean force(final Runnable task) {
            if (getThreadPoolExecutor().isShutdown()) {
                throw new RejectedExecutionException("Executor没有运行，不能加入到队列");
            }
            return offer(task);
        }
    }
}