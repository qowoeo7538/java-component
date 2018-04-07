package org.shaw.io.support;

import java.nio.ByteBuffer;

/**
 * @create: 2017-12-19
 * @description: 缓存读取处理
 */
@FunctionalInterface
public interface ReadProcess {

    /**
     * 缓存读取处理
     *
     * @param buffer 处理缓存
     */
    void onProcess(ByteBuffer buffer);
}
