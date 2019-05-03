package org.lucas.task.support;

@FunctionalInterface
public interface BeforeFunction {

    /**
     * 任务执行之前
     *
     * @param task   当前任务
     * @param thread 异常信息
     */
    void beforeExecute(final Runnable task, final Thread thread);

}
