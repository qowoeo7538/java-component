package org.shaw.task.support;

import org.shaw.task.StandardThreadExecutor;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.RejectedExecutionException;

/**
 * {@code BlockingQueue} 存取锁，会导致性能低下，
 * 通过 {@code LinkedTransferQueue} 预占模式，保证更好的性能
 */
public class ExecutorQueue extends LinkedTransferQueue<Runnable> {

    private static final long serialVersionUID = -7367811491338117333L;

    private StandardThreadExecutor standardThreadExecutor;

    public ExecutorQueue() {
        super();
    }

    public void setStandardThreadExecutor(StandardThreadExecutor standardThreadExecutor) {
        this.standardThreadExecutor = standardThreadExecutor;
    }

    /**
     * 将任务放入队列
     *
     * @param task {@code Runnable}
     * @return {@code boolean} 是否放入成功
     */
    public boolean force(Runnable task) {
        if (standardThreadExecutor.getThreadPoolExecutor().isShutdown()) {
            throw new RejectedExecutionException("Executor没有运行，不能加入到队列");
        }
        return offer(task);
    }

    /**
     * 优先扩充线程到maxThread，
     * <p>
     * 默认IO密集型处理，本身就是RPC项目，对IO要求较高
     *
     * @param task {@code Runnable} 任务
     * @return {@code boolean} 是否放入成功
     */
    @Override
    public boolean offer(Runnable task) {
        // 当前线程数量
        int poolSize = standardThreadExecutor.getThreadPoolExecutor().getPoolSize();

        if (poolSize == standardThreadExecutor.getThreadPoolExecutor().getMaximumPoolSize()) {
            // 当前线程数量是最大时，将任务加入队列
            return super.offer(task);
        }
        if (standardThreadExecutor.getConcurrencyCount() <= poolSize) {
            // 当前任务数量等于当前线程数量，将任务加入队列
            return super.offer(task);
        }
        if (poolSize < standardThreadExecutor.getThreadPoolExecutor().getMaximumPoolSize()) {
            // 当前线程数量小于最大线程数量，不加入队列。
            return false;
        }
        return super.offer(task);
    }
}
