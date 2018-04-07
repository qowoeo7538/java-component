package org.shaw.util;

import java.io.PrintWriter;
import java.io.StringWriter;

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
     * 将信息转化为 toString
     */
    public static String toString(Throwable e) {
        StringWriter w = new StringWriter();
        try (PrintWriter p = new PrintWriter(w)) {
            p.print(e.getClass().getName() + ": ");
            if (e.getMessage() != null) {
                p.print(e.getMessage() + "\n");
            }
            p.println();
            e.printStackTrace(p);
            return w.toString();
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

}
