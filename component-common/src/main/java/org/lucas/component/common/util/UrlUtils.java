package org.lucas.component.common.util;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @create: 2018-05-30
 * @description:
 */
public abstract class UrlUtils {

    public static <T> String getParamsString(final Map<String, T> params) {
        return getParamsString(params, StandardCharsets.UTF_8);
    }

    public static <T> String getParamsString(final Map<String, T> params, final Charset enc) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, T> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), enc));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue().toString(), enc));
            result.append("&");
        }
        String resultString = result.toString();
        return resultString.length() > 0
                ? resultString.substring(0, resultString.length() - 1)
                : resultString;
    }
}
