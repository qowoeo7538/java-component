package org.shaw.compiler;

/**
 * @create: 2018-03-04
 * @description:
 */
public interface Compiler {
    /**
     * @param code        源代码
     * @param classLoader 类加载器
     * @return Class
     */
    Class<?> compile(String code, ClassLoader classLoader);
}
