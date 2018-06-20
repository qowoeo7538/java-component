package org.shaw.util;

/**
 * Class 工具类
 */
public abstract class ClassUtils extends org.springframework.util.ClassUtils {

    /** The package separator String: "." */
    public static final String PACKAGE_SEPARATOR = ".";

    /** java文件后缀 */
    public static final String JAVA_FILE_SUFFIX = ".java";

    /** 默认类加载器类名 */
    public static final String APP_CLASSLOADER = "sun.misc.Launcher$AppClassLoader";

    /**
     * @param caller class 对象
     * @return 返回该 class 对象的类加载器
     */
    public static ClassLoader getCallerClassLoader(final Class<?> caller) {
        return caller.getClassLoader();
    }

    /**
     * @param packages  包名
     * @param className 类名
     * @return class 类型
     * @see #forName(String[], String, ClassLoader)
     */
    public static Class<?> forName(final String[] packages, final String className) {
        return forName(packages, className, null);
    }

    /**
     * 通过匹配所有的包名,尝试加载类.
     *
     * @param packages    包名
     * @param className   类名
     * @param classLoader 加载类
     * @return class 类型
     */
    public static Class<?> forName(final String[] packages, final String className, final ClassLoader classLoader) {
        try {
            return forName(className, classLoader);
        } catch (final ClassNotFoundException e) {
            if (packages != null && packages.length > 0) {
                // 匹配所有的导入包,尝试加载
                for (String pkg : packages) {
                    try {
                        return forName(pkg + PACKAGE_SEPARATOR + className, classLoader);
                    } catch (ClassNotFoundException e2) {
                    }
                }
            }
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
