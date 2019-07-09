package org.lucas.component.common.core;

import java.util.regex.Pattern;

public abstract class PatternConstants {

    /** 将字符串按 "," 分割 */
    public static final Pattern COMMA_SPLIT_PATTERN = Pattern.compile("\\s*[,]+\\s*");

}
