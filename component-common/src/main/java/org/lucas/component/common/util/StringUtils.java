package org.lucas.component.common.util;


import org.springframework.lang.Nullable;

/**
 * @create: 2018-06-01
 * @description:
 */
public abstract class StringUtils {

    /**
     * @param camelName 字符串
     * @param split     分割符号
     * @return
     */
    public static String camelToSplitName(final String camelName, final String split) {
        if (org.springframework.util.StringUtils.isEmpty(camelName)) {
            return camelName;
        }
        StringBuilder buf = null;
        for (int i = 0; i < camelName.length(); i++) {
            char ch = camelName.charAt(i);
            if (ch >= 'A' && ch <= 'Z') {
                if (buf == null) {
                    buf = new StringBuilder();
                    if (i > 0) {
                        buf.append(camelName.substring(0, i));
                    }
                }
                if (i > 0) {
                    buf.append(split);
                }
                buf.append(Character.toLowerCase(ch));
            } else if (buf != null) {
                buf.append(ch);
            }
        }
        return buf == null ? camelName : buf.toString();
    }

    public static boolean isEmpty(@Nullable Object str) {
        return (str == null || "".equals(str));
    }

}
