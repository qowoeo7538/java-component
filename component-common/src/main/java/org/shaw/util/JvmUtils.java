package org.shaw.util;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.*;

/**
 * JVM运行时跟踪工具
 */
public abstract class JvmUtils {
    public static void jstack(OutputStream stream) throws IOException {
        // 虚拟机线程系统管理
        ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
        /**
         * 返回所有活动线程的线程信息，并带有堆栈跟踪和同步信息。
         *
         * @param lockedMonitors: synchronized(Object obj)
         * @param lockedSynchronizers: 常指 ReentrantLock 和 ReentrantReadWriteLock 锁
         */
        ThreadInfo[] threadInfos = threadMxBean.dumpAllThreads(true, true);
        for (ThreadInfo threadInfo : threadInfos) {
            stream.write(getThreadDumpString(threadInfo).getBytes());
        }
    }

    /**
     * 返回线程信息
     *
     * @param threadInfo {@code ThreadInfo}
     * @return 日志信息
     */
    private static String getThreadDumpString(ThreadInfo threadInfo) {
        StringBuilder sb = new StringBuilder("\"" + threadInfo.getThreadName() + "\"" +
                " Id=" + threadInfo.getThreadId() + " " +
                threadInfo.getThreadState());
        // 获取线程等待锁
        if (threadInfo.getLockName() != null) {
            sb.append(" on " + threadInfo.getLockName());
        }
        // 获取阻塞对象
        if (threadInfo.getLockOwnerName() != null) {
            sb.append(" owned by \"" + threadInfo.getLockOwnerName() +
                    "\" Id=" + threadInfo.getLockOwnerId());
        }
        // 线程是否挂起
        if (threadInfo.isSuspended()) {
            sb.append(" (suspended)");
        }
        // 该线程是否执行JNI
        if (threadInfo.isInNative()) {
            sb.append(" (in native)");
        }
        sb.append('\n');
        int i = 0;
        // 获取线程栈信息
        StackTraceElement[] stackTrace = threadInfo.getStackTrace();
        MonitorInfo[] lockedMonitors = threadInfo.getLockedMonitors();
        for (; i < stackTrace.length && i < 32; i++) {
            // 最后执行的任务 --- 最开始执行的任务
            StackTraceElement ste = stackTrace[i];
            sb.append("\tat " + ste.toString());
            sb.append('\n');
            // 最后执行任务是否被锁
            if (i == 0 && threadInfo.getLockInfo() != null) {
                Thread.State ts = threadInfo.getThreadState();
                switch (ts) {
                    // 受阻塞并且正在等待监视器锁
                    case BLOCKED:
                        sb.append("\t-  blocked on " + threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    // 某一等待线程的线程状态
                    case WAITING:
                        sb.append("\t-  waiting on " + threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    // 具有指定等待时间的某一等待线程的线程状态
                    case TIMED_WAITING:
                        sb.append("\t-  waiting on " + threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    default:
                }
            }
            // 锁对象信息
            for (MonitorInfo mi : lockedMonitors) {
                if (mi.getLockedStackDepth() == i) {
                    sb.append("\t-  locked " + mi);
                    sb.append('\n');
                }
            }
        }
        if (i < stackTrace.length) {
            sb.append("\t...");
            sb.append('\n');
        }
        LockInfo[] locks = threadInfo.getLockedSynchronizers();
        if (locks.length > 0) {
            sb.append("\n\tNumber of locked synchronizers = " + locks.length);
            sb.append('\n');
            for (LockInfo li : locks) {
                sb.append("\t- " + li);
                sb.append('\n');
            }
        }
        sb.append('\n');
        return sb.toString();
    }
}
