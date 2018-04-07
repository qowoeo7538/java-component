package org.shaw.compiler.support;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @create: 2018-03-18
 * @description:
 */
public abstract class ClassHelper {
    /**
     * 通过 name 转成 URI
     *
     * @param name 资源名
     * @return URI
     */
    public static URI toURI(String name) {
        try {
            return new URI(name);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
