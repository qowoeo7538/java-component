package org.shaw.util;

import org.shaw.io.UnsafeStringWriter;

import java.io.PrintWriter;

/**
 * 关于异常的工具类.
 *
 * @author calvin
 * @version 2013-01-15
 */
public abstract class ExceptionUtils {

    /**
     * 将 CheckedException 转换为 UncheckedException.
     */
    public static RuntimeException unchecked(Exception e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        } else {
            return new RuntimeException(e);
        }
    }

    /**
     * 判断异常是否由某些底层的异常引起.
     */
    public static boolean isCausedBy(Exception ex, Class<? extends Exception>... causeExceptionClasses) {
        Throwable cause = ex.getCause();
        while (cause != null) {
            for (Class<? extends Exception> causeClass : causeExceptionClasses) {
                if (causeClass.isInstance(cause)) {
                    return true;
                }
            }
            cause = cause.getCause();
        }
        return false;
    }

    /**
     * 异常打印
     *
     * @param e 异常对象
     * @return {@code String} 打印信息
     * @see UnsafeStringWriter#mBuffer
     */
    public static String toString(Throwable e) {
        UnsafeStringWriter w = new UnsafeStringWriter();
        // p 本质就是对 {@link UnsafeStringWriter#mBuffer} 操作
        try (PrintWriter p = new PrintWriter(w)) {
            p.print(e.getClass().getName());
            if (e.getMessage() != null) {
                p.print(": " + e.getMessage());
            }
            p.println();
            e.printStackTrace(p);
            return w.toString();
        }
    }

}
