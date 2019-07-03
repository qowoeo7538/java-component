package org.lucas.component.common.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

/**
 * @create: 2018-05-30
 * @description:
 */
public abstract class UrlUtils {

    public static <T> String getParamsString(final Map<String, T> params)
            throws UnsupportedEncodingException {
        return getParamsString(params, "UTF-8");
    }

    public static <T> String getParamsString(final Map<String, T> params, final String enc)
            throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        for (Iterator<Map.Entry<String, T>> iterator = params.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, T> entry = iterator.next();
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
