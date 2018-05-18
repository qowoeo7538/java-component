package org.shaw.util;

import org.springframework.util.ClassUtils;

/**
 * Class 工具类
 */
public abstract class ClassHelper extends ClassUtils {

    public static final String JAVA_FILE_SUFFIX = ".java";

    /** 默认类加载器类名 */
    public final static String APP_CLASSLOADER = "sun.misc.Launcher$AppClassLoader";

    /**
     * @param caller class 对象
     * @return 返回该 class 对象的类加载器
     */
    public static ClassLoader getCallerClassLoader(Class<?> caller) {
        return caller.getClassLoader();
    }
}
