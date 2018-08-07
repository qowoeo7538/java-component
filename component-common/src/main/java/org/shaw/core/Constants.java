package org.shaw.core;

import java.util.regex.Pattern;

/**
 * 常量类
 */
public abstract class Constants {

    /** 核心数量 */
    public static final int CORE_SIZE = Runtime.getRuntime().availableProcessors();

    public static final String TRUE = "true";

    /** 默认组别名 */
    public static final String GROUP_KEY = "group";

    /** 接口 */
    public static final String INTERFACE_KEY = "interface";

    /** 版本 */
    public static final String VERSION_KEY = "version";

    /** 默认 key 前缀 */
    public static final String DEFAULT_KEY_PREFIX = "default.";

    /** win 系统前缀 */
    public static final String WINDOWS_SYS_PREFIX = "win";

    // ==============================

    /** 将字符串按 "," 分割 */
    public static final Pattern COMMA_SPLIT_PATTERN = Pattern
            .compile("\\s*[,]+\\s*");
}
