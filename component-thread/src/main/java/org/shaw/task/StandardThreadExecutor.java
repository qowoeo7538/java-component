package org.shaw.task;

import org.shaw.core.Constants;
import org.shaw.task.support.AbortPolicyWithReport;
import org.shaw.task.support.AfterFunction;
import org.shaw.task.support.BeforeFunction;
import org.shaw.task.support.DefaultFuture;
import org.shaw.task.support.ThrottleSupport;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * StandardThreadExecutor execute执行策略：	优先扩充线程到maxThread，再offer到queue，如果满了就reject
 * <p>
 * 适应场景：  适用于低于CPU运行效率的业务
 * <p>
 * 建议线程数目 = （（线程等待时间+线程处理时间）/线程处理时间 ）* CPU数目
 */
public class StandardThreadExecutor {

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
     * @see ThreadPoolTaskExecutor#corePoolSize
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
     * @see ThreadPoolTaskExecutor#maxPoolSize
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
     * 是否需要对线程池中子线程进行立即关闭
     */
    private boolean waitForTasksToCompleteOnShutdown = false;

    /**
     * 线程池超时关闭时间
     */
    private int awaitTerminationSeconds = 0;

    private BeforeFunction before = (r, t) -> {
    };

    private AfterFunction after = (r, t) -> {
    };

    public StandardThreadExecutor() {
        this(DEFAULT_CORE_POOL_SIZE, DEFAULT_MAX_POOL_SIZE);
    }

    public StandardThreadExecutor(final int corePoolSize, final int maxPoolSize) {
        this(corePoolSize, maxPoolSize, DEFAULT_KEEP_ALIVE_SECONDS);
    }

    public StandardThreadExecutor(final int corePoolSize, final int maxPoolSize, final int keepAliveSeconds) {
        this(corePoolSize, maxPoolSize, keepAliveSeconds, maxPoolSize);
    }

    public StandardThreadExecutor(final int corePoolSize, final int maxPoolSize, final int keepAliveSeconds, final int queueCapacity) {
        this(corePoolSize, maxPoolSize, keepAliveSeconds, queueCapacity, Executors.defaultThreadFactory());
    }

    public StandardThreadExecutor(final int corePoolSize, final int maxPoolSize, final int keepAliveSeconds, final int queueCapacity, final ThreadFactory threadFactory) {
        this(corePoolSize, maxPoolSize, keepAliveSeconds, queueCapacity, threadFactory, new AbortPolicyWithReport());
    }

    public StandardThreadExecutor(final int corePoolSize, final int maxPoolSize, final int keepAliveSeconds, final int queueCapacity, final ThreadFactory threadFactory, final RejectedExecutionHandler rejectedExecutionHandler) {
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

    public void setWaitForTasksToCompleteOnShutdown(final boolean waitForJobsToCompleteOnShutdown) {
        this.waitForTasksToCompleteOnShutdown = waitForJobsToCompleteOnShutdown;
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

    public void execute(final Runnable task) {
        this.throttleSupport.beforeAccess(task);
        this.execute(new ConcurrencyThrottlingRunnable(task));
    }

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

    public Future<?> submit(final Runnable task) {
        this.throttleSupport.beforeAccess(task);
        return this.threadPoolExecutor.submit(new ConcurrencyThrottlingRunnable(task));
    }

    public void destroy() {
        if (this.threadPoolExecutor != null) {
            if (this.waitForTasksToCompleteOnShutdown) {
                this.threadPoolExecutor.shutdown();
            } else {
                for (Runnable remainingTask : this.threadPoolExecutor.shutdownNow()) {
                    cancelRemainingTask(remainingTask);
                }
            }
            awaitTerminationIfNecessary(this.threadPoolExecutor);
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

        /**
         * 优先扩充线程到maxThread，
         * <p>
         * 返回 false 线程池将尝试扩充到最大线程个数，如果满载，则使用拒绝策略。
         *
         * @param task {@code Runnable} 任务
         * @return {@code boolean} 如果为 true，成功添加到队列。
         */
        @Override
        public boolean offer(final Runnable task) {
            // 线程池当前线程数量
            int poolSize = getThreadPoolExecutor().getPoolSize();

            if (poolSize < getThreadPoolExecutor().getMaximumPoolSize()) {
                // 当前线程数量小于最大线程数量，不加入队列。
                return false;
            }
            return super.offer(task);
        }
    }
}