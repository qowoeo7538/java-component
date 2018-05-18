package org.shaw.util;

import org.springframework.util.ResourceUtils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @create: 2018-05-18
 * @description:
 */
public abstract class ResourceHelper extends ResourceUtils {

    public static URI toURI(String location) {
        try {
            return ResourceUtils.toURI(location);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
