package org.shaw.compiler.support;

import org.shaw.compiler.Compiler;
import org.shaw.util.ExceptionUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @create: 2018-03-15
 * @description:
 */
public abstract class AbstractCompiler implements Compiler {

    /** 包名匹配正则 */
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+([$_a-zA-Z][$_a-zA-Z0-9\\.]*);");

    /** 类名匹配正则 */
    private static final Pattern CLASS_PATTERN = Pattern.compile("class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s+");

    /** 代码结束符 */
    private static final String CODE_ENDS_WITH = "}";

    /**
     * 加载源代码
     *
     * @param code        源代码
     * @param classLoader 类加载器
     * @return Class
     */
    @Override
    public Class<?> compile(String code, ClassLoader classLoader) {
        code = code.trim();
        /**
         * 判断 code 中是否包含 package、class 关键字。
         *
         * pkg: 包名
         * cls：类名
         */
        Matcher matcher = PACKAGE_PATTERN.matcher(code);
        String pkg;
        if (matcher.find()) {
            pkg = matcher.group(1);
        } else {
            pkg = "";
        }
        // 判断 code 中是否包含 class 关键字
        matcher = CLASS_PATTERN.matcher(code);
        String cls;
        if (matcher.find()) {
            cls = matcher.group(1);
        } else {
            throw new IllegalArgumentException("没有类名： " + code);
        }
        String className = pkg != null && pkg.length() > 0 ? pkg + "." + cls : cls;
        try {
            // 尝试加载 class 文件到内存
            return Class.forName(className, true, getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            if (!code.endsWith(CODE_ENDS_WITH)) {
                throw new IllegalStateException("java code 应该以 \"}\" 结尾, code: \n" + code + "\n");
            }
            try {
                // 尝试通过 className 和 code 编译文件并且加载
                return doCompile(className, code);
            } catch (RuntimeException t) {
                throw t;
            } catch (Throwable t) {
                throw new IllegalStateException("编译java文件失败, 原因: " + t.getMessage() + ", class: " + className
                        + ", code: \n" + code + "\n, stack: " + ExceptionUtils.toString(t));
            }
        }
    }

    /**
     * 对 java 文件进行编译
     *
     * @param name   类名
     * @param source 源代码
     * @return Class
     * @throws Throwable
     */
    protected abstract Class<?> doCompile(String name, String source) throws Throwable;

}
