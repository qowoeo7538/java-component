package org.lucas.component.common.util;

/**
 * @create: 2018-06-20
 * @description:
 */
public abstract class ConfigUtils {

    public static final String JDK_VERSION;

    static {
        try {
            final String currentVersion = System.getProperty("java.version");
            if (currentVersion.startsWith("1.8")) {
                JDK_VERSION = "1.8";
            } else if (currentVersion.startsWith("11")) {
                JDK_VERSION = "11";
            } else {
                // 默认 1.8
                JDK_VERSION = "1.8";
            }
        } catch (final Exception e) {
            throw new RuntimeException("不能获取当前 JDK 版本 ", e);
        }
    }

    public static boolean isEmpty(final String value) {
        return value == null || value.length() == 0
                || "false".equalsIgnoreCase(value)
                || "0".equalsIgnoreCase(value)
                || "null".equalsIgnoreCase(value)
                || "N/A".equalsIgnoreCase(value);
    }

}
