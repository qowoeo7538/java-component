package org.lucas.extension;

import org.lucas.component.common.util.Holder;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Author: shaw
 * @Date: 2019/6/24 16:53
 */
public abstract class AbstractLoader<T> {

    /**
     * 默认 SPI 目录
     */
    private static final String SERVICES_DIRECTORY = "META-INF/services/";

    /**
     * 自定义 SPI 目录
     */
    private static final String DIRECTORY = "META-INF/component/";

    /**
     * 自定义 SPI 目录
     */
    private static final String INTERNAL_DIRECTORY = DIRECTORY + "internal/";

    /**
     * 构建过的对象, 将保存到该对象中进行缓存 Map:{Class 对象 -> ExtensionLoader}
     */
    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();


    /**
     * {@code Holder} 维护一个 Map:{type 实现类的别名 -> 实现类的 Class}
     */
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    /**
     * 尝试从 {@link #EXTENSION_LOADERS} 获取 {@code ExtensionLoader}，
     * 如果为 {@code null}, 再构造该 Class 的 {@code ExtensionLoader} 放入 {@link #EXTENSION_LOADERS}
     *
     * @param type 类类型对象
     * @return {@code ExtensionLoader<T>}
     */
    public static <T> ExtensionLoader<T> getExtensionLoader(final Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("参数[type]不能为null!");
        }
        if (!(type.isInterface() || Modifier.isAbstract(type.getModifiers()))) {
            throw new IllegalArgumentException(type + " 必须是接口或抽象类!");
        }
        if (!withExtensionAnnotation(type)) {
            throw new IllegalArgumentException(type + " 不是一个SPI接口类, 没有 @SPI 注解!");
        }
        // 尝试获取该 type 的 ExtensionLoader 对象
        ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        if (loader == null) {
            // 构建 ExtensionLoader(type) 对象
            // 如果 key 存在则不做修改
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<T>(type));
            loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return loader;
    }

    /**
     * 双重检查
     * 获取 {@link #cachedClasses}
     *
     * @see #loadExtensionClasses()
     */
    protected Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.get();
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = loadExtensionClasses();
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    /**
     * 判断该方法是否包含 SPI 注解
     *
     * @param type 类类型对象
     * @param <T>
     * @return 如果包含 SPI 注解则返回 {@code true}
     */
    private static <T> boolean withExtensionAnnotation(Class<T> type) {
        return type.isAnnotationPresent(SPI.class);
    }

}
