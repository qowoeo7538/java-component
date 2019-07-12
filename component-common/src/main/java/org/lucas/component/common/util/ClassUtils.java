package org.lucas.component.common.util;

import org.lucas.component.common.core.constants.ClassConstants;
import org.springframework.util.Assert;

import java.io.Closeable;
import java.io.Externalizable;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class 工具类
 */
public abstract class ClassUtils {

    /**
     * 继承分隔符： {@code '$'}.
     */
    private static final char INNER_CLASS_SEPARATOR = '$';

    // =====================================

    /**
     * 基础数据类型
     * 装箱类型 -> 拆箱类型
     */
    private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new IdentityHashMap<>(8);

    /**
     * 基础数据类型
     * 拆箱类型 -> 装箱类型
     */
    private static final Map<Class<?>, Class<?>> primitiveTypeToWrapperMap = new IdentityHashMap<>(8);

    /**
     * 包含基础数据类型和基础数据类型数组及void.
     * class.getName -> class
     */
    private static final Map<String, Class<?>> primitiveTypeNameMap = new HashMap<>(32);

    /**
     * String--> Class 缓存
     */
    public static final ConcurrentHashMap<String, Class> CLASS_CACHE = new ConcurrentHashMap<>();

    /**
     * Class --> String 缓存
     */
    public static final ConcurrentHashMap<Class, String> TYPE_STR_CACHE = new ConcurrentHashMap<>();

    /**
     * 常用的类类型注册，包含：
     * 1.基础包装类型；
     * 2.基础包装类型数组；
     * 3.数字，字符串，Object类型及数组：Number.class, Number[].class, String.class, String[].class, Class.class, Class[].class, Object.class, Object[].class；
     * 4.异常类型：Throwable.class, Exception.class, RuntimeException.class, Error.class, StackTraceElement.class, StackTraceElement[].class；
     * 5.枚举，迭代器，集合等类型：Enum.class, Iterable.class, Iterator.class, Enumeration.class, Collection.class, List.class, Set.class, Map.class, Map.Entry.class, Optional.class
     * 6.序列化等类型：Serializable.class, Externalizable.class, Closeable.class, AutoCloseable.class, Cloneable.class, Comparable.class
     */
    private static final Map<String, Class<?>> commonClassCache = new HashMap<>(64);

    static {
        primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
        primitiveWrapperTypeMap.put(Byte.class, byte.class);
        primitiveWrapperTypeMap.put(Character.class, char.class);
        primitiveWrapperTypeMap.put(Double.class, double.class);
        primitiveWrapperTypeMap.put(Float.class, float.class);
        primitiveWrapperTypeMap.put(Integer.class, int.class);
        primitiveWrapperTypeMap.put(Long.class, long.class);
        primitiveWrapperTypeMap.put(Short.class, short.class);

        for (Iterator<Map.Entry<Class<?>, Class<?>>> iterator = primitiveWrapperTypeMap.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<Class<?>, Class<?>> entry = iterator.next();
            primitiveTypeToWrapperMap.put(entry.getValue(), entry.getKey());
            registerCommonClasses(entry.getKey());
        }

        Set<Class<?>> primitiveTypes = new HashSet<>(32);

        primitiveTypes.addAll(primitiveWrapperTypeMap.values());
        Collections.addAll(primitiveTypes, boolean[].class, byte[].class, char[].class,
                double[].class, float[].class, int[].class, long[].class, short[].class);

        primitiveTypes.add(void.class);
        for (Class<?> primitiveType : primitiveTypes) {
            primitiveTypeNameMap.put(primitiveType.getName(), primitiveType);
        }

        registerCommonClasses(Boolean[].class, Byte[].class, Character[].class, Double[].class,
                Float[].class, Integer[].class, Long[].class, Short[].class);
        registerCommonClasses(Number.class, Number[].class, String.class, String[].class,
                Class.class, Class[].class, Object.class, Object[].class);
        registerCommonClasses(Throwable.class, Exception.class, RuntimeException.class,
                Error.class, StackTraceElement.class, StackTraceElement[].class);
        registerCommonClasses(Enum.class, Iterable.class, Iterator.class, Enumeration.class,
                Collection.class, List.class, Set.class, Map.class, Map.Entry.class, Optional.class);

        Class<?>[] javaLanguageInterfaceArray = {Serializable.class, Externalizable.class,
                Closeable.class, AutoCloseable.class, Cloneable.class, Comparable.class};
        registerCommonClasses(javaLanguageInterfaceArray);
    }

    public static String getTypeStr(Class clazz) {
        String str = TYPE_STR_CACHE.get(clazz);
        if (StringUtils.isEmpty(str)) {

        }
        return "";
    }

    public static String[] getTypeStrs(Class... types) {
        return null;
    }

    public static String[] getTypeStrs(Class[] types, boolean javaStyle) {
        return null;
    }

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
            return ClassUtils.forName(className, classLoader);
        } catch (final ClassNotFoundException e) {
            if (packages != null && packages.length > 0) {
                // 匹配所有的导入包,尝试加载
                for (String pkg : packages) {
                    try {
                        return ClassUtils.forName(pkg + ClassConstants.PACKAGE_SEPARATOR + className, classLoader);
                    } catch (ClassNotFoundException e2) {
                    }
                }
            }
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static Class<?> forName(String name, ClassLoader classLoader)
            throws ClassNotFoundException, LinkageError {
        Assert.notNull(name, "Name must not be null");

        // 获取原始数据类类型
        Class<?> clazz = resolvePrimitiveClassName(name);
        if (clazz == null) {
            // 从常用类型中获取。
            clazz = commonClassCache.get(name);
        }
        if (clazz != null) {
            return clazz;
        }

        // 例如："java.lang.String[]"
        if (name.endsWith(ClassConstants.ARRAY_SUFFIX)) {
            // 获取数组的类型
            String elementClassName = name.substring(0, name.length() - ClassConstants.ARRAY_SUFFIX.length());
            Class<?> elementClass = forName(elementClassName, classLoader);
            // 实例化数组类类型
            return Array.newInstance(elementClass, 0).getClass();
        }

        // 非原始数据类型数组"[Ljava.lang.String;"
        if (name.startsWith(ClassConstants.NON_PRIMITIVE_ARRAY_PREFIX) && name.endsWith(";")) {
            String elementName = name.substring(ClassConstants.NON_PRIMITIVE_ARRAY_PREFIX.length(), name.length() - 1);
            Class<?> elementClass = forName(elementName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        // "[[I" or "[[Ljava.lang.String;"
        if (name.startsWith(ClassConstants.INTERNAL_ARRAY_PREFIX)) {
            String elementName = name.substring(ClassConstants.INTERNAL_ARRAY_PREFIX.length());
            Class<?> elementClass = forName(elementName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        ClassLoader clToUse = classLoader;
        if (clToUse == null) {
            clToUse = getDefaultClassLoader();
        }
        try {
            return Class.forName(name, false, clToUse);
        } catch (ClassNotFoundException ex) {
            int lastDotIndex = name.lastIndexOf(ClassConstants.PACKAGE_SEPARATOR);
            if (lastDotIndex != -1) {
                String innerClassName =
                        name.substring(0, lastDotIndex) + INNER_CLASS_SEPARATOR + name.substring(lastDotIndex + 1);
                try {
                    return Class.forName(innerClassName, false, clToUse);
                } catch (ClassNotFoundException ex2) {
                    // Swallow - let original exception get through
                }
            }
            throw ex;
        }
    }

    /**
     * 注册为常用类型
     *
     * @param commonClasses 常用类型
     */
    private static void registerCommonClasses(Class<?>... commonClasses) {
        for (Class<?> clazz : commonClasses) {
            commonClassCache.put(clazz.getName(), clazz);
        }
    }

    /**
     * 通过原始数据类型名称获取类类型
     *
     * @param name 原始数据类型名称
     * @return 原始数据类类型
     */
    public static Class<?> resolvePrimitiveClassName(String name) {
        Class<?> result = null;
        // 除了基本数据类型，大多数类名都较长，因此比较长度是值得的。
        if (name != null && name.length() <= 8) {
            result = primitiveTypeNameMap.get(name);
        }
        return result;
    }

    /**
     * @param lhsType 父类
     * @param rhsType 子类
     * @return 判断 rhsType 是否可转换 lhsType（或 lhsType 子类）
     */
    public static boolean isAssignable(Class<?> lhsType, Class<?> rhsType) {
        Assert.notNull(lhsType, "Left-hand side type must not be null");
        Assert.notNull(rhsType, "Right-hand side type must not be null");
        // rhsType 是否可转换 lhsType
        if (lhsType.isAssignableFrom(rhsType)) {
            return true;
        }
        // 基础类型拆箱和装箱比较
        if (lhsType.isPrimitive()) {
            Class<?> resolvedPrimitive = primitiveWrapperTypeMap.get(rhsType);
            if (lhsType == resolvedPrimitive) {
                return true;
            }
        } else {
            Class<?> resolvedWrapper = primitiveTypeToWrapperMap.get(rhsType);
            if (resolvedWrapper != null && lhsType.isAssignableFrom(resolvedWrapper)) {
                return true;
            }
        }
        return false;
    }

    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = ClassUtils.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }
        return cl;
    }

    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        return (clazz.isPrimitive() || isPrimitiveWrapper(clazz));
    }

    public static boolean isPrimitiveWrapper(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        return primitiveWrapperTypeMap.containsKey(clazz);
    }
}
