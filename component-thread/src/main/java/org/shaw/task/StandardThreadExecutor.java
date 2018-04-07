package org.shaw.task;

import org.shaw.task.support.AbortPolicyWithReport;
import org.shaw.task.support.ExecutorQueue;
import org.shaw.task.support.ThrottleSupport;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

/**
 * StandardThreadExecutor execute执行策略：	优先扩充线程到maxThread，再offer到queue，如果满了就reject
 * <p>
 * 适应场景：  适用于低于CPU运行效率的业务
 * <p>
 * 建议线程数目 = （（线程等待时间+线程处理时间）/线程处理时间 ）* CPU数目
 */
public class StandardThreadExecutor extends ThreadPoolTaskExecutor {

    /** 核心数量 */
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
     * 阻塞任务队列容量(默认为int的最大值)
     *
     * @see ThreadPoolTaskExecutor#queueCapacity
     */
    private static final int DEFAULT_QUEUE_CAPACITY = 1;

    /**
     * 线程池中超过核心线程数目的空闲线程最大存活时间；
     * 可以allowCoreThreadTimeOut(true)使得核心线程有效时间
     */
    private static final int DEFAULT_KEEP_ALIVE_SECONDS = 60;

    /** 线程额外对象 */
    private final ConcurrencyThrottleAdapter throttleSupport = new ConcurrencyThrottleAdapter();

    public StandardThreadExecutor() {
        this(DEFAULT_CORE_POOL_SIZE, DEFAULT_MAX_POOL_SIZE);
    }

    public StandardThreadExecutor(int corePoolSize, int maxPoolSize) {
        this(corePoolSize, maxPoolSize, DEFAULT_KEEP_ALIVE_SECONDS);
    }

    public StandardThreadExecutor(int corePoolSize, int maxPoolSize, int keepAliveSeconds) {
        this(corePoolSize, maxPoolSize, keepAliveSeconds, DEFAULT_QUEUE_CAPACITY);
    }

    public StandardThreadExecutor(int corePoolSize, int maxPoolSize, int keepAliveSeconds, int queueCapacity) {
        this(corePoolSize, maxPoolSize, keepAliveSeconds, queueCapacity, new CustomizableThreadFactory());
    }

    public StandardThreadExecutor(int corePoolSize, int maxPoolSize, int keepAliveSeconds, int queueCapacity, ThreadFactory threadFactory) {
        this(corePoolSize, maxPoolSize, keepAliveSeconds, queueCapacity, threadFactory, new AbortPolicyWithReport());
    }

    public StandardThreadExecutor(int corePoolSize, int maxPoolSize, int keepAliveSeconds, int queueCapacity, ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {
        super.setCorePoolSize(corePoolSize);
        // 最大线程数不能小于核心线程数
        super.setMaxPoolSize(maxPoolSize > corePoolSize ? maxPoolSize : corePoolSize);
        super.setKeepAliveSeconds(keepAliveSeconds);
        super.setThreadFactory(threadFactory);
        super.setRejectedExecutionHandler(rejectedExecutionHandler);
        // 设置最大并发数 maxPoolSize + queueCapacity
        throttleSupport.setConcurrencyLimit(maxPoolSize + queueCapacity);
        initialize();
    }

    @Override
    public void execute(Runnable task) {
        throttleSupport.beforeAccess(task);
        super.execute(() -> {
            try {
                task.run();
            } catch (RejectedExecutionException rx) {
                // 尝试放入队列，失败则直接使用失败策略
                if (!((ExecutorQueue) super.getThreadPoolExecutor().getQueue()).force(task)) {
                    super.getThreadPoolExecutor().getRejectedExecutionHandler().rejectedExecution(task, super.getThreadPoolExecutor());
                }
            } finally {
                throttleSupport.afterAccess();
            }
        });
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        FutureTask<T> futureTask = new FutureTask<>(task);
        throttleSupport.beforeAccess(futureTask);
        try {
            super.execute(futureTask);
        } catch (RejectedExecutionException rx) {
            // 尝试放入队列，失败则直接使用失败策略
            if (!((ExecutorQueue) getThreadPoolExecutor().getQueue()).force(futureTask)) {
                getThreadPoolExecutor().getRejectedExecutionHandler().rejectedExecution(futureTask, getThreadPoolExecutor());
            }
        } finally {
            throttleSupport.afterAccess();
        }
        return futureTask;
    }

    @Override
    protected BlockingQueue<Runnable> createQueue(int queueCapacity) {
        ExecutorQueue executorQueue = new ExecutorQueue();
        executorQueue.setStandardThreadExecutor(this);
        return executorQueue;
    }

    /**
     * 获取运行任务数量
     *
     * @return
     */
    public int getConcurrencyCount() {
        return throttleSupport.getConcurrencyCount().get();
    }

    private class ConcurrencyThrottleAdapter extends ThrottleSupport {

        /** 并发量设置 */
        private int concurrencyLimit;

        /**
         * 运行前
         * <p>
         * 默认队列没有长度限制，因此这里进行控制
         *
         * @see ThrottleSupport#beforeAccess(Runnable)
         */
        @Override
        protected void runBefore(Runnable task, int concurrencyCount) {
            // 因为队列没有长度，所以在这里进行并发控制
            if (concurrencyCount > concurrencyLimit) {
                getConcurrencyCount().decrementAndGet();
                /**
                 * @see java.util.concurrent.ThreadPoolExecutor.AbortPolicy#rejectedExecution(Runnable, ThreadPoolExecutor)
                 */
                getThreadPoolExecutor().getRejectedExecutionHandler().rejectedExecution(task, getThreadPoolExecutor());
            }
        }

        /**
         * 设置最大并发
         *
         * @param concurrencyLimit
         */
        protected void setConcurrencyLimit(int concurrencyLimit) {
            this.concurrencyLimit = concurrencyLimit;
        }
    }

}