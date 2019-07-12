package org.lucas.component.common.compiler.support;

import org.lucas.component.common.compiler.Compiler;
import org.lucas.component.common.core.constants.ClassConstants;
import org.lucas.component.common.util.ExceptionUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zx
 * @create: 2018-03-15
 * @description: 编译工具基类
 */
public abstract class AbstractCompiler implements Compiler {

    /**
     * 包名匹配正则
     */
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+([$_a-zA-Z][$_a-zA-Z0-9\\.]*);");

    /**
     * 类名匹配正则
     */
    private static final Pattern CLASS_PATTERN = Pattern.compile("class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s+");


    /**
     * 根据源代码和类加载器加载类
     *
     * @param code        源代码
     * @param classLoader 类加载器
     * @return Class {@code Class<?>} 类对象
     */
    @Override
    public Class<?> compile(String code, final ClassLoader classLoader) {
        code = code.trim();
        // pkg: 包名
        Matcher matcher = PACKAGE_PATTERN.matcher(code);
        String pkg;
        if (matcher.find()) {
            pkg = matcher.group(1);
        } else {
            pkg = "";
        }
        // cls：类名
        matcher = CLASS_PATTERN.matcher(code);
        String cls;
        if (matcher.find()) {
            cls = matcher.group(1);
        } else {
            throw new IllegalArgumentException("没有类名： " + code);
        }
        // 类全名
        String className = pkg != null && pkg.length() > 0 ? pkg + "." + cls : cls;
        try {
            // 尝试加载 class 文件到内存
            return Class.forName(className, true, getClass().getClassLoader());
        } catch (final ClassNotFoundException e) {
            if (!code.endsWith(ClassConstants.CODE_ENDS_WITH)) {
                throw new IllegalStateException("java code 应该以 \"}\" 结尾, code: \n" + code + "\n");
            }
            try {
                // 尝试通过 className 和 code 编译文件并且加载
                return doCompile(className, code);
            } catch (final RuntimeException t) {
                throw t;
            } catch (final Throwable t) {
                throw new IllegalStateException("编译java文件失败, 原因: " + t.getMessage() + ", class: " + className
                        + ", code: \n" + code + "\n, stack: " + ExceptionUtils.toString(t));
            }
        }
    }

    /**
     * 对 java 文件进行编译
     *
     * @param name   类全名
     * @param source 源代码
     * @return Class {@code Class<?>} 类对象
     * @throws Throwable
     */
    protected abstract Class<?> doCompile(final String name, final String source) throws Throwable;

}
