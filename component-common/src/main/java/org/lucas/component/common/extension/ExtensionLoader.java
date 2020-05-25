package org.lucas.component.common.extension;

import org.lucas.component.common.core.collect.ConcurrentHashSet;
import org.lucas.component.common.core.constants.CommonConstants;
import org.lucas.component.common.core.lang.SplitPatternPool;
import org.lucas.component.common.util.ClassUtils;
import org.lucas.component.common.util.CollectionUtils;
import org.lucas.component.common.util.ExceptionUtils;
import org.lucas.component.common.util.Holder;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ExtensionLoader<T> {

    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();

    /**
     * {实现类 Class -> Class 对象的实例}
     */
    private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    // =============================

    private final ConcurrentMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();

    private final ExtensionFactory objectFactory;

    /**
     * {@code Holder} 维护一个 Object 对象
     */
    private final Holder<Object> cachedAdaptiveInstance = new Holder<>();

    /**
     * SPI实现类的 Class -> 实现类的别名
     */
    private final ConcurrentMap<Class<?>, String> cachedNames = new ConcurrentHashMap<>();

    /**
     * SPI 接口
     */
    private final Class<?> type;

    /**
     * 包含 @Adaptive 注解的实现类
     */
    private volatile Class<?> cachedAdaptiveClass = null;

    /**
     * 为保证错误及时获取，通过 volatile 每次获取主内存的值
     */
    private volatile Throwable createAdaptiveInstanceError;

    private Map<String, IllegalStateException> exceptions = new ConcurrentHashMap<>();

    private Set<Class<?>> cachedWrapperClasses;

    /**
     * 默认实现类的别名 (SPI接口注解的 value 信息设置)
     */
    private String cachedDefaultName;

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
     * {@code Holder} 维护一个 Map:{type 实现类的别名 -> 实现类的 Class}
     */
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    /**
     * ExtensionLoader<type> 基于 ExtensionLoader<ExtensionFactory> 构建,
     * 所以会先构建 ExtensionLoader<ExtensionFactory> 对象
     *
     * @param type 类类型
     */
    private ExtensionLoader(final Class<?> type) {
        this.type = type;
        // 创建对象的 SPI 工厂类
        objectFactory = (type == ExtensionFactory.class ? null : ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension());
    }

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
     * 设计模式: 单例双重检查
     */
    public T getAdaptiveExtension() {
        Object instance = cachedAdaptiveInstance.get();
        if (instance == null) {
            if (createAdaptiveInstanceError == null) {
                synchronized (cachedAdaptiveInstance) {
                    instance = cachedAdaptiveInstance.get();
                    if (instance == null) {
                        try {
                            instance = createAdaptiveExtension();
                            cachedAdaptiveInstance.set(instance);
                        } catch (Throwable t) {
                            createAdaptiveInstanceError = t;
                            throw new IllegalStateException("fail to create adaptive instance: " + t.toString(), t);
                        }
                    }
                }
            } else {
                throw new IllegalStateException("fail to create adaptive instance: " + createAdaptiveInstanceError.toString(), createAdaptiveInstanceError);
            }
        }
        return (T) instance;
    }

    /**
     * @param name
     * @param clazz
     * @see #getExtensionClasses()
     */
    public void addExtension(String name, Class<?> clazz) {
        getExtensionClasses();
        if (!ClassUtils.isAssignable(type, clazz)) {
            throw new IllegalStateException("输入类型 " +
                    clazz + " 没有实现 " + type);
        }
        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            throw new IllegalStateException("输入类型 " +
                    clazz + " 不能是接口或抽象类!");
        }
        if (!clazz.isAnnotationPresent(Adaptive.class)) {
            if (StringUtils.isEmpty(name)) {
                throw new IllegalStateException("Extension name is blank (Extension " + type + ")!");
            }
            // 判断该别名是否已经被其它实现类使用
            if (cachedClasses.get().containsKey(name)) {
                throw new IllegalStateException("Extension name " +
                        name + " already existed(Extension " + type + ")!");
            }
            // 加入到缓存信息中
            cachedNames.put(clazz, name);
            cachedClasses.get().put(name, clazz);
        } else {
            if (cachedAdaptiveClass != null) {
                throw new IllegalStateException("Adaptive Extension already existed(Extension " + type + ")!");
            }
            cachedAdaptiveClass = clazz;
        }
    }

    /**
     * 根据类类型获取别名
     *
     * @param extensionClass SPI实现类类型
     * @return 别名
     */
    public String getExtensionName(Class<?> extensionClass) {
        getExtensionClasses();
        return cachedNames.get(extensionClass);
    }

    public T getExtension(String name) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException(EXTENSION_IS_NULL_EXCEPTION);
        }
        if (CommonConstants.TRUE.equals(name)) {
            return getDefaultExtension();
        }
        final Holder<Object> holder = getOrCreateHolder(name);
        Object instance = holder.get();
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    /**
     * @return 获取默认的 SPI 实现对象
     */
    public T getDefaultExtension() {
        getExtensionClasses();
        if (null == cachedDefaultName || cachedDefaultName.length() == 0 || CommonConstants.TRUE.equals(cachedDefaultName)) {
            return null;
        }
        return getExtension(cachedDefaultName);
    }

    public Set<String> getSupportedExtensions() {
        Map<String, Class<?>> clazzes = getExtensionClasses();
        return Collections.unmodifiableSet(new TreeSet<>(clazzes.keySet()));
    }

    /**
     * @return 默认的别名, 如果返回 {@code null} 则没有配置
     */
    public String getDefaultExtensionName() {
        getExtensionClasses();
        return cachedDefaultName;
    }

    public boolean hasExtension(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException(EXTENSION_IS_NULL_EXCEPTION);
        }
        Class<?> c = this.getExtensionClass(name);
        return c != null;
    }

    private Holder<Object> getOrCreateHolder(String name) {
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
        return holder;
    }

    /**
     * 双重检查
     * 获取 {@link #cachedClasses}
     *
     * @see #loadExtensionClasses()
     */
    private Map<String, Class<?>> getExtensionClasses() {
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
     * 根据扩展类别名加载实例对象
     *
     * @param name 扩展类别名
     * @see #getExtensionClasses()
     */
    private T createExtension(String name) {
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw findException(name);
        }
        try {
            T instance = (T) EXTENSION_INSTANCES.get(clazz);
            // 因为 ConcurrentMap 原子操作的特性, 此处也能像双重检查一样保证单例
            if (instance == null) {
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.getConstructor().newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            }
            injectExtension(instance);
            Set<Class<?>> wrapperClasses = cachedWrapperClasses;
            if (!CollectionUtils.isEmpty(wrapperClasses)) {
                for (Class<?> wrapperClass : wrapperClasses) {
                    instance = injectExtension((T) wrapperClass.getConstructor(type).newInstance(instance));
                }
            }
            return instance;
        } catch (Throwable t) {
            throw new IllegalStateException("Extension instance(name: " + name + ", class: " +
                    type + ")  could not be instantiated: " + t.getMessage(), t);
        }
    }

    private IllegalStateException findException(String name) {
        for (Iterator<Map.Entry<String, IllegalStateException>> iterator = exceptions.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, IllegalStateException> entry = iterator.next();
            if (entry.getKey().toLowerCase().contains(name.toLowerCase())) {
                return entry.getValue();
            }
        }
        StringBuilder buf = new StringBuilder("No such extension " + type.getName() + " by name " + name);
        int i = 1;
        for (Iterator<Map.Entry<String, IllegalStateException>> iterator = exceptions.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, IllegalStateException> entry = iterator.next();
            if (i == 1) {
                buf.append(", possible causes: ");
            }

            buf.append("\r\n(");
            buf.append(i++);
            buf.append(") ");
            buf.append(entry.getKey());
            buf.append(":\r\n");
            buf.append(ExceptionUtils.toString(entry.getValue()));
        }
        return new IllegalStateException(buf.toString());
    }

    /**
     * @return 实例对象 T
     * @see #getAdaptiveExtensionClass()
     */
    private T createAdaptiveExtension() {
        try {
            /**
             * 注意：通过 {@link Class#newInstance()} 创建实例，会绕过编译时的异常检查。如果不希望如此，
             *      建议通过反射构造函数来创建实例 {@link java.lang.reflect.Constructor#newInstance(Object...)}
             */
            return injectExtension((T) getAdaptiveExtensionClass().getConstructor().newInstance());
        } catch (final Exception e) {
            throw new IllegalStateException("不能创建 adaptive extension: " + type + ", 原因: " + e.getMessage(), e);
        }
    }

    private Class<?> getAdaptiveExtensionClass() {
        getExtensionClasses();
        return cachedAdaptiveClass;
    }

    /**
     * 尝试根据别名进行加载类
     *
     * @param name 别名
     * @return {@code true} 加载成功
     */
    private Class<?> getExtensionClass(String name) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type == null");
        }
        if (name == null) {
            throw new IllegalArgumentException(EXTENSION_IS_NULL_EXCEPTION);
        }
        return getExtensionClasses().get(name);
    }


    /**
     * 读取的 SPI 目录下对应的文件,加载 Class.
     *
     * @return Map:{别名 -> Class}
     * @see #loadDirectory(Map, String, String)
     */
    private Map<String, Class<?>> loadExtensionClasses() {
        cacheDefaultExtensionName();

        final Map<String, Class<?>> extensionClasses = new HashMap<>(16);
        loadDirectory(extensionClasses, INTERNAL_DIRECTORY, type.getName());
        loadDirectory(extensionClasses, DIRECTORY, type.getName());
        loadDirectory(extensionClasses, SERVICES_DIRECTORY, type.getName());
        return extensionClasses;
    }

    private void cacheDefaultExtensionName() {
        // 获取 SPI 接口的注解信息
        final SPI defaultAnnotation = type.getAnnotation(SPI.class);
        if (defaultAnnotation != null) {
            String value = defaultAnnotation.value();
            if ((value = value.trim()).length() > 0) {
                // 将 SPI 注解信息的 value 按照 "," 进行切割
                String[] names = SplitPatternPool.COMMA_SPLIT_PATTERN.split(value);
                if (names.length > 1) {
                    throw new IllegalStateException("默认扩展名超过最大长度[1] " + type.getName() + ": " + Arrays.toString(names));
                }
                if (names.length == 1) {
                    cachedDefaultName = names[0];
                }
            }
        }
    }

    /**
     * 根据 {@link #type} 找到 SPI 目录的描述文件,加载相关的 Class
     *
     * @param extensionClasses Map:{别名 -> Class}
     * @param dir              SPI 文件目录
     * @param type             接口文件名称
     */
    private void loadDirectory(final Map<String, Class<?>> extensionClasses, final String dir, final String type) {
        // 获取对应的 SPI 接口描述文件
        final String fileName = dir + type;
        try {
            Enumeration<URL> urls;
            final ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
            // 加载 SPI 接口描述文件
            if (classLoader != null) {
                urls = classLoader.getResources(fileName);
            } else {
                // 如果 ClassLoader 为空则通过静态方法加载 SPI 目录下的描述文件
                urls = ClassLoader.getSystemResources(fileName);
            }
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL resourceURL = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceURL);
                }
            }
        } catch (Throwable t) {
            // todo 日志
        }
    }

    /**
     * 根据路径解析配置文件
     *
     * @param extensionClasses Map:{别名 -> Class}
     * @param classLoader      类加载器
     * @param resourceURL      class类的资源路径
     */
    private void loadResource(final Map<String, Class<?>> extensionClasses, final ClassLoader classLoader, final URL resourceURL) {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(resourceURL.openStream(), "utf-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 注释标识
                final int ci = line.indexOf('#');
                if (ci >= 0) {
                    line = line.substring(0, ci);
                }
                line = line.trim();
                if (line.length() > 0) {
                    try {
                        String name = null;
                        // 分割符
                        final int i = line.indexOf('=');
                        if (i > 0) {
                            // 别名
                            name = line.substring(0, i).trim();
                            // 类全名
                            line = line.substring(i + 1).trim();
                        }
                        if (line.length() > 0) {
                            loadClass(extensionClasses, resourceURL, Class.forName(line, true, classLoader), name);
                        }
                    } catch (final Throwable t) {
                        IllegalStateException e = new IllegalStateException("加载扩展器失败(接口: " + type + ", class line: " + line + ") in " + resourceURL + ", cause: " + t.getMessage(), t);
                        exceptions.put(line, e);
                    }
                }
            }
        } catch (final Throwable t) {
            // todo 日志
        }
    }

    /**
     * 保存加载的 class 信息(别名、Class).
     *
     * @param extensionClasses Map:{别名 -> Class}
     * @param resourceURL      资源路径
     * @param clazz            类
     * @param name             别名
     * @throws NoSuchMethodException
     */
    private void loadClass(final Map<String, Class<?>> extensionClasses, final URL resourceURL, final Class<?> clazz, String name) throws NoSuchMethodException {
        if (!ClassUtils.isAssignable(type, clazz)) {
            throw new IllegalStateException("Error when load extension class(interface: " +
                    type + ", class line: " + clazz.getName() + "), class "
                    + clazz.getName() + "is not subtype of interface.");
        }
        // 判断是否包含 @Adaptive 注解
        if (clazz.isAnnotationPresent(Adaptive.class)) {
            cacheAdaptiveClass(clazz);
            // 判断是否包含 type 类型的构造函数
        } else if (isWrapperClass(clazz)) {
            cacheWrapperClass(clazz);
        } else {
            // 需要一个无参构造器
            clazz.getConstructor();
            if (StringUtils.isEmpty(name)) {
                // 默认类名为别名
                name = findAnnotationName(clazz);
                if (name.length() == 0) {
                    throw new IllegalStateException("No such extension name for the class " + clazz.getName() + " in the config " + resourceURL);
                }
            }
            final String[] names = SplitPatternPool.COMMA_SPLIT_PATTERN.split(name);
            if (names != null && names.length > 0) {
                for (String n : names) {
                    // 缓存名称
                    cacheName(clazz, n);
                    saveInExtensionClass(extensionClasses, clazz, name);
                }
            }
        }
    }

    private void saveInExtensionClass(Map<String, Class<?>> extensionClasses, Class<?> clazz, String name) {
        Class<?> c = extensionClasses.get(name);
        if (c == null) {
            extensionClasses.put(name, clazz);
        } else if (c != clazz) {
            throw new IllegalStateException("Duplicate extension " + type.getName() + " name " + name + " on " + c.getName() + " and " + clazz.getName());
        }
    }

    private void cacheName(Class<?> clazz, String name) {
        if (!cachedNames.containsKey(clazz)) {
            cachedNames.put(clazz, name);
        }
    }

    /**
     * 获取默认别名
     *
     * @param clazz 类类型
     * @return 默认别名
     */
    private String findAnnotationName(Class<?> clazz) {
        String name = clazz.getSimpleName();
        if (name.endsWith(type.getSimpleName())) {
            name = name.substring(0, name.length() - type.getSimpleName().length());
        }
        return name.toLowerCase();
    }

    /**
     * 缓存适配类
     *
     * @param clazz 类类型
     */
    private void cacheAdaptiveClass(Class<?> clazz) {
        if (cachedAdaptiveClass == null) {
            cachedAdaptiveClass = clazz;
        } else if (!cachedAdaptiveClass.equals(clazz)) {
            throw new IllegalStateException("More than 1 adaptive class found: "
                    + cachedAdaptiveClass.getClass().getName()
                    + ", " + clazz.getClass().getName());
        }
    }

    private void cacheWrapperClass(Class<?> clazz) {
        if (cachedWrapperClasses == null) {
            cachedWrapperClasses = new ConcurrentHashSet<>();
        }
        cachedWrapperClasses.add(clazz);
    }

    /**
     * 动态生成类
     *
     * @param instance 类
     * @return 动态生成类
     */
    private T injectExtension(final T instance) {
        try {
            if (objectFactory != null) {
                // 遍历 instance 所有的 public 方法
                for (Method method : instance.getClass().getMethods()) {
                    if (isSetter(method)) {
                        if (method.getAnnotation(DisableInject.class) != null) {
                            continue;
                        }
                        Class<?> pt = method.getParameterTypes()[0];
                        if (ClassUtils.isPrimitiveOrWrapper(pt)) {
                            continue;
                        }
                        try {
                            String property = getSetterProperty(method);
                            Object object = objectFactory.getExtension(pt, property);
                            if (object != null) {
                                method.invoke(instance, object);
                            }
                        } catch (final Exception e) {
                            //TODO 日志
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (final Exception e) {
            //TODO 日志
            e.printStackTrace();
        }
        return instance;
    }

    private String getSetterProperty(Method method) {
        return method.getName().length() > 3 ? method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4) : "";
    }

    /**
     * 条件：
     * 1) 方法名以 set 开头
     * 2) 方法只有一个参数
     * 3) 方法的修饰符为 public
     *
     * @param method 方法类型
     * @return 是否是setter方法。
     */
    private boolean isSetter(Method method) {
        return method.getName().startsWith("set")
                && method.getParameterTypes().length == 1
                && Modifier.isPublic(method.getModifiers());
    }

    /**
     * @param clazz 构造器参数类型
     * @return 是否包含 {@link #type} 类型的构造器
     */
    private boolean isWrapperClass(final Class<?> clazz) {
        try {
            clazz.getConstructor(type);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
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

    private static final String EXTENSION_IS_NULL_EXCEPTION = "Extension name == null";
}
