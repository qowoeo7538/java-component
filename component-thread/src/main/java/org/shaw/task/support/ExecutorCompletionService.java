package org.shaw.task.support;

import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

public class ExecutorCompletionService<V> implements CompletionService<V> {
    private final Executor executor;
    private final AbstractExecutorService aes;
    private final BlockingQueue<Future<V>> completionQueue;
    private final ThrottleSupport throttleSupport;

    private class QueueingFuture extends FutureTask<Void> {
        QueueingFuture(RunnableFuture<V> task) {
            super(task, null);
            this.task = task;
        }

        @Override
        protected void done() {
            completionQueue.add(task);
        }

        private final Future<V> task;
    }

    public ExecutorCompletionService(Executor executor, ThrottleSupport throttleSupport) {
        if (executor == null) {
            throw new NullPointerException();
        }
        this.executor = executor;
        this.aes = (executor instanceof AbstractExecutorService) ?
                (AbstractExecutorService) executor : null;
        this.completionQueue = new LinkedTransferQueue<>();
        this.throttleSupport = throttleSupport;
    }

    @Override
    public Future<V> submit(Callable<V> task) {
        if (task == null) {
            throw new NullPointerException();
        }
        RunnableFuture<V> f = newTaskFor(task);
        throttleSupport.beforeAccess(f);
        executor.execute(new QueueingFuture(f));
        return f;
    }

    @Override
    public Future<V> submit(Runnable task, V result) {
        if (task == null) {
            throw new NullPointerException();
        }
        RunnableFuture<V> f = newTaskFor(task, result);
        throttleSupport.beforeAccess(f);
        executor.execute(new QueueingFuture(f));
        return f;
    }

    @Override
    public Future<V> take() throws InterruptedException {
        return completionQueue.take();
    }

    @Override
    public Future<V> poll() {
        return completionQueue.poll();
    }

    @Override
    public Future<V> poll(long timeout, TimeUnit unit)
            throws InterruptedException {
        return completionQueue.poll(timeout, unit);
    }

    private RunnableFuture<V> newTaskFor(Callable<V> task) {
        return new FutureTask<>(task);
    }

    private RunnableFuture<V> newTaskFor(Runnable task, V result) {
        return new FutureTask<>(task, result);
    }
}
