package org.lucas.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

/**
 * @Author: shaw
 * @Date: 2019/5/17 15:02
 */
public abstract class UnsafeUtils {

    private static final Unsafe THE_UNSAFE;

    static {
        try {
            // 根据安全策略, 获取 Unsafe
            final PrivilegedExceptionAction<Unsafe> action = () -> {
                // 通过反射进行加载
                Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                theUnsafe.setAccessible(true);
                return (Unsafe) theUnsafe.get(null);
            };
            THE_UNSAFE = AccessController.doPrivileged(action);
        } catch (final Exception e) {
            throw new RuntimeException("不能加载 Unsafe ", e);
        }
    }

    /**
     * @return {@code Unsafe}
     */
    public static Unsafe getUnsafe() {
        return THE_UNSAFE;
    }

}
