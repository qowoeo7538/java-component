package org.lucas.util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @create: 2018-05-18
 * @description:
 */
public abstract class ResourceUtils {

    public static URI toURI(final String location) {
        try {
            return org.springframework.util.ResourceUtils.toURI(location);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
