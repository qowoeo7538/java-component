package org.lucas.component.common.core.pattern;

import java.util.regex.Pattern;

public abstract class SplitPattern {

    /** 将字符串按 "," 分割 */
    public static final Pattern COMMA_SPLIT_PATTERN = Pattern.compile("\\s*[,]+\\s*");

}
