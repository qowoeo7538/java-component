package org.shaw.task.support;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class ExecutorConfigurationSupport {

    private final ExecutorService executor = getExecutor();

    /**
     * 是否需要对线程池中子线程进行立即关闭
     */
    private boolean waitForTasksToCompleteOnShutdown = false;

    /**
     * 线程池超时关闭时间
     */
    private int awaitTerminationSeconds = 0;

    public void destroy() {
        if (this.executor != null) {
            if (this.waitForTasksToCompleteOnShutdown) {
                this.executor.shutdown();
            } else {
                for (Runnable remainingTask : this.executor.shutdownNow()) {
                    cancelRemainingTask(remainingTask);
                }
            }
            awaitTerminationIfNecessary(this.executor);
        }
    }

    public void setWaitForTasksToCompleteOnShutdown(final boolean waitForJobsToCompleteOnShutdown) {
        this.waitForTasksToCompleteOnShutdown = waitForJobsToCompleteOnShutdown;
    }

    /**
     * 尝试取消剩下的 {@code Future} 任务
     *
     * @param task 任务
     */
    protected void cancelRemainingTask(final Runnable task) {
        if (task instanceof Future) {
            // 尝试取消任务，如果任务已经启动，通过 mayInterruptIfRunning 参数来终止该任务。
            ((Future<?>) task).cancel(true);
        }
    }

    /**
     * @return 获取执行器
     */
    protected abstract ExecutorService getExecutor();

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
}
