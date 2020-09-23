package org.lucas.component.common.core.idcenter.support;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class NamingThreadFactory implements ThreadFactory {

    /**
     * Thread name pre
     */
    private String name;

    /**
     * Is daemon thread
     */
    private boolean daemon;

    /**
     * UncaughtExceptionHandler
     */
    private UncaughtExceptionHandler uncaughtExceptionHandler;

    /**
     * Sequences for multi thread name prefix
     */
    private final ConcurrentHashMap<String, AtomicLong> sequences;

    public NamingThreadFactory() {
        this(null, false, null);
    }

    public NamingThreadFactory(String name) {
        this(name, false, null);
    }

    public NamingThreadFactory(String name, boolean daemon) {
        this(name, daemon, null);
    }

    public NamingThreadFactory(String name, boolean daemon, Thread.UncaughtExceptionHandler handler) {
        this.name = name;
        this.daemon = daemon;
        this.uncaughtExceptionHandler = handler;
        this.sequences = new ConcurrentHashMap<>();
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(this.daemon);
        String prefix = this.name;
        if (StringUtils.isBlank(prefix)) {
            prefix = getInvoker(2);
        }

        thread.setName(prefix + "-" + getSequence(prefix));

        // no specified uncaughtExceptionHandler, just do logging.
        if (this.uncaughtExceptionHandler != null) {
            thread.setUncaughtExceptionHandler(this.uncaughtExceptionHandler);
        } else {
            thread.setUncaughtExceptionHandler((t, e) ->
                    // 日志
                    System.out.println("unhandled exception in thread: " + t.getId() + ":" + t.getName())
            );
        }
        return thread;
    }

    /**
     * Get the method invoker's class name
     *
     * @param depth
     * @return
     */
    private String getInvoker(int depth) {
        Exception e = new Exception();
        // 1 获取堆栈信息
        StackTraceElement[] stes = e.getStackTrace();
        if (stes.length > depth) {
            // 2 获取调用者的类名
            return ClassUtils.getShortClassName(stes[depth].getClassName());
        }
        return getClass().getSimpleName();
    }

    /**
     * Get sequence for different naming prefix
     *
     * @param invoker
     * @return
     */
    private long getSequence(String invoker) {
        AtomicLong r = this.sequences.get(invoker);
        if (r == null) {
            r = new AtomicLong(0);
            AtomicLong previous = this.sequences.putIfAbsent(invoker, r);
            if (previous != null) {
                r = previous;
            }
        }
        return r.incrementAndGet();
    }

}
