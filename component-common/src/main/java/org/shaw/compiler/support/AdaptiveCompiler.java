package org.shaw.compiler.support;

import org.shaw.compiler.Compiler;
import org.shaw.core.extension.ExtensionLoader;

/**
 * @create: 2018-03-15
 * @description:
 */
public class AdaptiveCompiler implements Compiler {

    /** 每次需要该参数时,都需要从主内存读取,保证可见性 */
    private static volatile String DEFAULT_COMPILER;

    public static void setDefaultCompiler(String compiler) {
        DEFAULT_COMPILER = compiler;
    }

    /**
     * 获取 {@code Compiler} 名为 {@link #DEFAULT_COMPILER} 的扩展类
     *
     * @param code
     * @param classLoader 类加载器
     * @return Class
     */
    @Override
    public Class<?> compile(String code, ClassLoader classLoader) {
        Compiler compiler;
        ExtensionLoader<Compiler> loader = ExtensionLoader.getExtensionLoader(Compiler.class);
        String name = DEFAULT_COMPILER;
        if (name != null && name.length() > 0) {
            compiler = loader.getExtension(name);
        } else {
            compiler = loader.getDefaultExtension();
        }
        return compiler.compile(code, classLoader);
    }
}
