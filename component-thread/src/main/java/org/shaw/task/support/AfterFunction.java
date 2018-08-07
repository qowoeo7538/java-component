package org.shaw.task.support;

@FunctionalInterface
public interface AfterFunction {

    /**
     * 任务执行之后
     *
     * @param task      当前任务
     * @param throwable 异常
     */
    void afterExecute(final Runnable task, final Throwable throwable);

}
