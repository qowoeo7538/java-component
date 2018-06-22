package org.shaw.util;

/**
 * @create: 2018-06-20
 * @description:
 */
public abstract class ConfigUtils {

    public static boolean isEmpty(final String value) {
        return value == null || value.length() == 0
                || "false".equalsIgnoreCase(value)
                || "0".equalsIgnoreCase(value)
                || "null".equalsIgnoreCase(value)
                || "N/A".equalsIgnoreCase(value);
    }

}
