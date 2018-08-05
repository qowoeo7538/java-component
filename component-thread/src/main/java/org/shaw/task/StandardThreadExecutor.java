package org.shaw.task;

import org.shaw.task.support.AbortPolicyWithReport;
import org.shaw.task.support.DefaultFuture;
import org.shaw.task.support.ThrottleSupport;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;

/**
 * StandardThreadExecutor execute执行策略：	优先扩充线程到maxThread，再offer到queue，如果满了就reject
 * <p>
 * 适应场景：  适用于低于CPU运行效率的业务
 * <p>
 * 建议线程数目 = （（线程等待时间+线程处理时间）/线程处理时间 ）* CPU数目
 */
public class StandardThreadExecutor extends ThreadPoolTaskExecutor {

    /**
     * 核心数量
     */
    public static final int CORE_SIZE = Runtime.getRuntime().availableProcessors();

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
    private static final int DEFAULT_CORE_POOL_SIZE = CORE_SIZE + 1;

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
    private static final int DEFAULT_MAX_POOL_SIZE = 2 * CORE_SIZE + 1;

    /**
     * 线程池中超过核心线程数目的空闲线程最大存活时间；
     * 可以allowCoreThreadTimeOut(true)使得核心线程有效时间
     */
    private static final int DEFAULT_KEEP_ALIVE_SECONDS = 60;

    /**
     * 线程池限流对象
     */
    private final ConcurrencyThrottleAdapter throttleSupport;

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
        this(corePoolSize, maxPoolSize, keepAliveSeconds, queueCapacity, new CustomizableThreadFactory());
    }

    public StandardThreadExecutor(final int corePoolSize, final int maxPoolSize, final int keepAliveSeconds, final int queueCapacity, final ThreadFactory threadFactory) {
        this(corePoolSize, maxPoolSize, keepAliveSeconds, queueCapacity, threadFactory, new AbortPolicyWithReport());
    }

    public StandardThreadExecutor(final int corePoolSize, final int maxPoolSize, final int keepAliveSeconds, final int queueCapacity, final ThreadFactory threadFactory, final RejectedExecutionHandler rejectedExecutionHandler) {
        super.setCorePoolSize(corePoolSize);
        // 最大线程数不能小于核心线程数
        super.setMaxPoolSize(maxPoolSize > corePoolSize ? maxPoolSize : corePoolSize);
        super.setKeepAliveSeconds(keepAliveSeconds);
        super.setThreadFactory(threadFactory);
        super.setRejectedExecutionHandler(rejectedExecutionHandler);
        initialize();

        throttleSupport = new ConcurrencyThrottleAdapter();
        // 设置最大并发数 maxPoolSize + queueCapacity
        throttleSupport.setConcurrencyLimit(maxPoolSize + queueCapacity);
    }

    @Override
    public void execute(final Runnable task) {
        throttleSupport.beforeAccess(task);
        super.execute(new ConcurrencyThrottlingRunnable(task));
    }

    @Override
    public <T> Future<T> submit(final Callable<T> task) {
        throttleSupport.beforeAccess(task);
        return super.submit(new ConcurrencyThrottlingCallable<>(task));
    }

    /**
     * @param task         任务
     * @param defaultValue 默认返回值
     * @return 如果超出并发设置，则使用默认返回值快速响应
     */
    public <T> Future<T> submit(final Callable<T> task, final T defaultValue) {
        if (throttleSupport.isLimit()) {
            return new DefaultFuture<>(defaultValue);
        }
        return super.submit(new ConcurrencyThrottlingCallable<>(task));
    }

    @Override
    public Future<?> submit(final Runnable task) {
        throttleSupport.beforeAccess(task);
        return super.submit(new ConcurrencyThrottlingRunnable(task));
    }

    @Override
    protected BlockingQueue<Runnable> createQueue(final int queueCapacity) {
        final ExecutorQueue executorQueue = new ExecutorQueue();
        return executorQueue;
    }

    private class ConcurrencyThrottleAdapter extends ThrottleSupport {

        public ConcurrencyThrottleAdapter() {
            super(getThreadPoolExecutor());
        }
    }

    private class ConcurrencyThrottlingRunnable implements Runnable {

        private final Runnable target;

        public ConcurrencyThrottlingRunnable(Runnable target) {
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

        public ConcurrencyThrottlingCallable(Callable<V> target) {
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
        public boolean force(Runnable task) {
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
        public boolean offer(Runnable task) {
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