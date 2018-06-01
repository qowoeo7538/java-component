package org.shaw.util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @create: 2018-05-18
 * @description:
 */
public abstract class ResourceUtils extends org.springframework.util.ResourceUtils {

    public static URI toURI(String location) {
        try {
            return org.springframework.util.ResourceUtils.toURI(location);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
