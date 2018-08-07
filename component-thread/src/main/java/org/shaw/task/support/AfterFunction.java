package org.shaw.task.support;

@FunctionalInterface
public interface AfterFunction {

    /**
     * 任务执行之后
     *
     * @param r 当前任务
     * @param t 异常信息
     */
    void afterExecute(final Runnable r, final Throwable t);

}
