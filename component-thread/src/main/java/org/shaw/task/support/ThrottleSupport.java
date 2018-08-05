package org.shaw.task.support;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @see org.springframework.util.ConcurrencyThrottleSupport
 */
public abstract class ThrottleSupport {

    /**
     * 并发量设置
     */
    private int concurrencyLimit;

    /**
     * 当前线程数量 （{@link ThreadPoolExecutor#getActiveCount()} 返回值并不准确）
     */
    private final AtomicInteger concurrencyCount;

    /**
     * 线程池
     */
    private final ThreadPoolExecutor executor;

    public ThrottleSupport(final ThreadPoolExecutor executor) {
        this.executor = executor;
        this.concurrencyCount = new AtomicInteger(0);
    }

    /**
     * 设置最大并发
     *
     * @param concurrencyLimit
     */
    public void setConcurrencyLimit(final int concurrencyLimit) {
        this.concurrencyLimit = concurrencyLimit;
    }

    /**
     * @return 当前任务数量
     */
    public int getConcurrencyCount() {
        return this.concurrencyCount.get();
    }

    /**
     * 运行前
     *
     * @param task 运行任务
     */
    public void beforeAccess(final Runnable task) {
        // 入口处控制限流量
        if (this.isLimit()) {
            executor.getRejectedExecutionHandler().rejectedExecution(task, executor);
        }
    }

    /**
     * 运行前
     *
     * @param task 运行任务
     */
    public <V> void beforeAccess(final Callable<V> task) {
        // 入口处控制限流量
        if (this.isLimit()) {
            executor.getRejectedExecutionHandler().rejectedExecution(new FutureTask<>(task), executor);
        }
    }

    /**
     * 添加一个任务，判断是否超过并发限制
     *
     * @return 如果 {@code true} 超过并发限制
     */
    public boolean isLimit() {
        // 入口处控制限流量
        if (this.concurrencyCount.incrementAndGet() > concurrencyLimit) {
            this.concurrencyCount.decrementAndGet();
            return true;
        }
        return false;
    }

    /**
     * 运行后
     */
    public void afterAccess() {
        this.concurrencyCount.decrementAndGet();
    }

}
