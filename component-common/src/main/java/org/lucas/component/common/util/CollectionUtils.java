package org.lucas.component.common.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @create: 2018-03-14
 * @description:
 */
public abstract class CollectionUtils {

    /**
     * 将偶数个 String 转换成 Map 对象
     *
     * @param pairs String
     */
    public static Map<String, String> toStringMap(final String... pairs) {
        Map<String, String> parameters = new HashMap<>(16);
        if (pairs.length > 0) {
            if (pairs.length % 2 != 0) {
                throw new IllegalArgumentException("个数必须为偶数个!");
            }
            for (int i = 0; i < pairs.length; i = i + 2) {
                parameters.put(pairs[i], pairs[i + 1]);
            }
        }
        return parameters;
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }

    public static boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

}
