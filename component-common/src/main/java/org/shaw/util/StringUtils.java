package org.shaw.util;

import org.shaw.io.UnsafeStringWriter;

import java.io.PrintWriter;

/**
 * String 工具类
 */
public abstract class StringUtils extends org.springframework.util.StringUtils {
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
