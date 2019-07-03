package org.lucas.component.common.compiler;

import org.lucas.extension.SPI;

/**
 * @create: 2018-03-04
 * @description: zx
 */
@SPI("javassist")
public interface Compiler {
    /**
     * @param code        源代码
     * @param classLoader 类加载器
     * @return Compiled class
     */
    Class<?> compile(String code, ClassLoader classLoader);
}
