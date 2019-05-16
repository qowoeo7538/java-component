package org.lucas.compiler.support;

import org.lucas.compiler.Compiler;
import org.lucas.extension.Adaptive;
import org.lucas.extension.ExtensionLoader;
import org.lucas.util.StringUtils;

/**
 * @create: 2018-03-15
 * @description:
 */
@Adaptive
public class AdaptiveCompiler implements Compiler {

    /**
     * 默认编译器(每次需要该参数时,都需要从主内存读取,保证并发时内存可见性)
     */
    private static volatile String DEFAULT_COMPILER;

    /**
     * 设置默认编译器
     *
     * @param compiler 编译器名
     */
    public static void setDefaultCompiler(final String compiler) {
        DEFAULT_COMPILER = compiler;
    }

    /**
     * 获取 {@code Compiler} 名为 {@link #DEFAULT_COMPILER} 的扩展类
     *
     * @param code
     * @param classLoader 类加载器
     * @return {@code Class<?>} 类对象
     */
    @Override
    public Class<?> compile(final String code, final ClassLoader classLoader) {
        Compiler compiler;
        final ExtensionLoader<Compiler> loader = ExtensionLoader.getExtensionLoader(Compiler.class);
        final String name = DEFAULT_COMPILER;
        if (StringUtils.isEmpty(name)) {
            // 默认编译器 javassist.
            compiler = loader.getDefaultExtension();
        } else {
            compiler = loader.getExtension(name);
        }
        return compiler.compile(code, classLoader);
    }
}
