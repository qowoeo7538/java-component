package org.shaw.task.support;

@FunctionalInterface
public interface BeforeFunction {

    /**
     * 任务执行之前
     *
     * @param r 当前任务
     * @param t 异常信息
     */
    void beforeExecute(final Runnable r, final Thread t);

}
