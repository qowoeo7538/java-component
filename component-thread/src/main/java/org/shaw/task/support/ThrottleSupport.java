package org.shaw.task.support;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @see org.springframework.util.ConcurrencyThrottleSupport
 */
public abstract class ThrottleSupport {

    /** 当前线程数量 （{@link ThreadPoolExecutor#getActiveCount()} 返回值并不准确） */
    private AtomicInteger concurrencyCount = new AtomicInteger(0);

    /**
     * 运行前
     *
     * @param task 运行任务
     */
    public void beforeAccess(Runnable task) {
        runBefore(task, concurrencyCount.incrementAndGet());
    }

    /**
     * 运行前处理
     *
     * @param task             当前任务
     * @param concurrencyCount 当前任务数量
     */
    protected abstract void runBefore(Runnable task, int concurrencyCount);

    /**
     * 运行后
     */
    public void afterAccess() {
        runAfter(this.concurrencyCount.decrementAndGet());
    }

    /**
     * 运行后处理
     *
     * @param concurrencyCount 当前任务数量
     */
    protected void runAfter(int concurrencyCount) {

    }

    /**
     * 返回当前任务数量
     *
     * @return
     */
    public AtomicInteger getConcurrencyCount() {
        return concurrencyCount;
    }
}
