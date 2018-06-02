package org.shaw.core.extension;

import org.shaw.compiler.Compiler;
import org.shaw.core.Constants;
import org.shaw.util.ConcurrentHashSet;
import org.shaw.util.ExceptionUtils;
import org.shaw.util.Holder;

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
import java.util.regex.Pattern;

public class ExtensionLoader<T> {

    /** 将字符串按 "," 分割 */
    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");

    /** 默认 SPI 目录 */
    private static final String SERVICES_DIRECTORY = "META-INF/services/";

    /** 自定义 SPI 目录 */
    private static final String DIRECTORY = "META-INF/component/";

    /** 自定义 SPI 目录 */
    private static final String INTERNAL_DIRECTORY = DIRECTORY + "internal/";

    // =============================

    /** 构建过的对象, 将保存到该对象中进行缓存 Map:{Class 对象 -> ExtensionLoader} */
    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();

    /** {实现类 Class -> Class 对象的实例} */
    private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    // =============================

    /** Map:{别名 -> 类的 @Activate 信息} */
    private final Map<String, Activate> cachedActivates = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();

    private final ExtensionFactory objectFactory;

    private final ConcurrentMap<Class<?>, String> cachedNames = new ConcurrentHashMap<Class<?>, String>();

    /** {@code Holder} 维护一个 Object 对象 */
    private final Holder<Object> cachedAdaptiveInstance = new Holder<>();

    /** {@code Holder} 维护一个 Map:{type 实现类的别名 -> 实现类的 Class} */
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    /** SPI 接口 */
    private final Class<?> type;

    /** 包含 @Adaptive 注解的实现类 */
    private volatile Class<?> cachedAdaptiveClass = null;

    /** 为保证错误及时获取，通过 volatile 每次获取主内存的值 */
    private volatile Throwable createAdaptiveInstanceError;

    private Map<String, IllegalStateException> exceptions = new ConcurrentHashMap<>();

    private Set<Class<?>> cachedWrapperClasses;

    /** 默认实现类的别名 (SPI接口注解的 value 信息设置) */
    private String cachedDefaultName;

    /**
     * ExtensionLoader<type> 基于 ExtensionLoader<ExtensionFactory> 构建,
     * 所以会先构建 ExtensionLoader<ExtensionFactory> 对象
     *
     * @param type 类类型
     */
    private ExtensionLoader(final Class<?> type) {
        this.type = type;
        // 创建对象的 SPI 工厂类,忽略掉本身就是工厂的 ExtensionFactory 类
        objectFactory = (type == ExtensionFactory.class ? null :
                ExtensionLoader
                        // 获取 ExtensionFactory 工厂的 ExtensionLoader 对象
                        .getExtensionLoader(ExtensionFactory.class)
                        .getAdaptiveExtension());
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
        if (!type.isInterface()) {
            throw new IllegalArgumentException(type + " 不是一个接口类!");
        }
        if (!withExtensionAnnotation(type)) {
            throw new IllegalArgumentException(type + " 不是一个扩展接口, 没有 @" + SPI.class.getSimpleName() + " 注解!");
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

    public T getExtension(String name) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Extension name == null");
        }
        if (Constants.TRUE.equals(name)) {
            return getDefaultExtension();
        }
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
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
        if (null == cachedDefaultName || cachedDefaultName.length() == 0 || Constants.TRUE.equals(cachedDefaultName)) {
            return null;
        }
        return getExtension(cachedDefaultName);
    }

    public Set<String> getSupportedExtensions() {
        Map<String, Class<?>> clazzes = getExtensionClasses();
        return Collections.unmodifiableSet(new TreeSet<>(clazzes.keySet()));
    }

    /**
     * 根据扩展类别名获取实例对象
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
            if (wrapperClasses != null && !wrapperClasses.isEmpty()) {
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
     * 判断该方法是否包含 SPI 注解
     *
     * @param type 类类型对象
     * @param <T>
     * @return 如果包含 SPI 注解则返回 {@code true}
     */
    private static <T> boolean withExtensionAnnotation(Class<T> type) {
        return type.isAnnotationPresent(SPI.class);
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

    /**
     * @see #getExtensionClasses()
     */
    private Class<?> getAdaptiveExtensionClass() {
        getExtensionClasses();
        if (cachedAdaptiveClass != null) {
            return cachedAdaptiveClass;
        }
        return cachedAdaptiveClass = createAdaptiveExtensionClass();
    }

    private Class<?> createAdaptiveExtensionClass() {
        String code = createAdaptiveExtensionClassCode();
        ClassLoader classLoader = findClassLoader();
        Compiler compiler = ExtensionLoader.getExtensionLoader(Compiler.class)
                .getAdaptiveExtension();
        return compiler.compile(code, classLoader);
    }

    /**
     * 通过反射获取 {@link #type} 的源代码
     *
     * @return Source code
     */
    private String createAdaptiveExtensionClassCode() {
        StringBuilder codeBuilder = new StringBuilder();
        Method[] methods = type.getMethods();
        boolean hasAdaptiveAnnotation = false;
        for (Method m : methods) {
            if (m.isAnnotationPresent(Adaptive.class)) {
                hasAdaptiveAnnotation = true;
                break;
            }
        }

        if (!hasAdaptiveAnnotation) {
            throw new IllegalStateException("No adaptive method on extension " + type.getName()
                    + ", refuse to create the adaptive class!");
        }

        /**
         * 生成 SPI 接口的内部 Adaptive 类
         *
         * package           : SPI 接口包名
         * import            : org.shaw.core.extension.ExtensionLoader
         * class 描述        : public class {@code type.getSimpleName()}$Adaptive implements {@code type.getCanonicalName()}
         */
        codeBuilder.append("package " + type.getPackage().getName() + ";");
        codeBuilder.append("\nimport " + ExtensionLoader.class.getName() + ";");
        codeBuilder.append("\npublic class " + type.getSimpleName() + "$Adaptive" + " implements "
                + type.getCanonicalName() + " {");

        for (Method method : methods) {
            // 获取返回类型
            Class<?> rt = method.getReturnType();
            // 获取参数类型
            Class<?>[] pts = method.getParameterTypes();
            // 获取方法异常类型
            Class<?>[] ets = method.getExceptionTypes();

            Adaptive adaptiveAnnotation = method.getAnnotation(Adaptive.class);
            StringBuilder code = new StringBuilder(512);
            if (adaptiveAnnotation == null) {
                code.append("throw new UnsupportedOperationException(\"method ")
                        .append(method.toString()).append(" of interface ")
                        .append(type.getName()).append(" is not adaptive method!\");");
            } else {
                // RUL 处理
                int urlTypeIndex = -1;
                for (int i = 0; i < pts.length; ++i) {
                    if (pts[i].equals(ExtURL.class)) {
                        urlTypeIndex = i;
                        break;
                    }
                }

                // 对 URL 参数进行空指针检查
                if (urlTypeIndex != -1) {
                    String s = String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"url == null\");",
                            urlTypeIndex);
                    code.append(s);

                    s = String.format("\n%s url = arg%d;", ExtURL.class.getName(), urlTypeIndex);
                    code.append(s);

                } else {
                    // 没有找到 URL.
                    String attribMethod = null;

                    // 在参数中寻找 URL.
                    LBL_PTS:
                    for (int i = 0; i < pts.length; ++i) {
                        Method[] ms = pts[i].getMethods();
                        for (Method m : ms) {
                            String name = m.getName();
                            if ((name.startsWith("get") || name.length() > 3)
                                    && Modifier.isPublic(m.getModifiers())
                                    && !Modifier.isStatic(m.getModifiers())
                                    && m.getParameterTypes().length == 0
                                    && m.getReturnType() == URL.class) {
                                urlTypeIndex = i;
                                attribMethod = name;
                                break LBL_PTS;
                            }
                        }
                    }
                    if (attribMethod == null) {
                        throw new IllegalStateException("fail to create adaptive class for interface " + type.getName()
                                + ": not found url parameter or url attribute in parameters of method " + method.getName());
                    }

                    // 空指针检查
                    String s = String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"%s argument == null\");",
                            urlTypeIndex, pts[urlTypeIndex].getName());
                    code.append(s);
                    s = String.format("\nif (arg%d.%s() == null) throw new IllegalArgumentException(\"%s argument %s() == null\");",
                            urlTypeIndex, attribMethod, pts[urlTypeIndex].getName(), attribMethod);
                    code.append(s);

                    // url 赋值
                    s = String.format("%s url = arg%d.%s();", URL.class.getName(), urlTypeIndex, attribMethod);
                    code.append(s);
                }

                String[] value = adaptiveAnnotation.value();
                // 默认 value
                if (value.length == 0) {
                    StringBuilder sb = new StringBuilder(128);
                    /**
                     * 类名转换, 如 ExtensionLoader 转成 Extension.loader
                     */
                    char[] charArray = type.getSimpleName().toCharArray();
                    for (int i = 0; i < charArray.length; i++) {
                        if (Character.isUpperCase(charArray[i])) {
                            if (i != 0) {
                                sb.append(".");
                            }
                            sb.append(Character.toLowerCase(charArray[i]));
                        } else {
                            sb.append(charArray[i]);
                        }
                    }
                    value = new String[]{sb.toString()};
                }

                String defaultExtName = cachedDefaultName;
                String getNameCode = null;
                for (int i = value.length - 1; i >= 0; --i) {
                    if (i == value.length - 1) {
                        if (null != defaultExtName) {
                            getNameCode = String.format("url.getParameter(\"%s\", \"%s\")", value[i], defaultExtName);
                        } else {
                            getNameCode = String.format("url.getParameter(\"%s\")", value[i]);
                        }
                    }else {
                        getNameCode = String.format("url.getParameter(\"%s\", %s)", value[i], getNameCode);
                    }
                }

                code.append("\nString extName = ").append(getNameCode).append(";");
                String s = String.format("\nif(extName == null) " +
                                "throw new IllegalStateException(\"Fail to get extension(%s) name from url(\" + url.toString() + \") use keys(%s)\");",
                        type.getName(), Arrays.toString(value));
                code.append(s);

                s = String.format("\n%s extension = (%<s)%s.getExtensionLoader(%s.class).getExtension(extName);",
                        type.getName(), ExtensionLoader.class.getSimpleName(), type.getName());
                code.append(s);

                if (!rt.equals(void.class)) {
                    code.append("\nreturn ");
                }

                s = String.format("extension.%s(", method.getName());
                code.append(s);
                for (int i = 0; i < pts.length; i++) {
                    if (i != 0) {
                        code.append(", ");
                    }
                    code.append("arg").append(i);
                }
                code.append(");");
            }

            codeBuilder.append("\npublic " + rt.getCanonicalName() + " " + method.getName() + "(");
            for (int i = 0; i < pts.length; i++) {
                if (i > 0) {
                    codeBuilder.append(", ");
                }
                codeBuilder.append(pts[i].getCanonicalName());
                codeBuilder.append(" ");
                codeBuilder.append("arg" + i);
            }
            codeBuilder.append(")");
            if (ets.length > 0) {
                codeBuilder.append(" throws ");
                for (int i = 0; i < ets.length; i++) {
                    if (i > 0) {
                        codeBuilder.append(", ");
                    }
                    codeBuilder.append(ets[i].getCanonicalName());
                }
            }
            codeBuilder.append(" {");
            codeBuilder.append(code.toString());
            codeBuilder.append("\n}");
        }
        codeBuilder.append("\n}");
        return codeBuilder.toString();
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
     * 读取的 SPI 目录下对应的文件,加载 Class.
     *
     * @return Map:{别名 -> Class}
     * @see #loadDirectory(Map, String)
     */
    private Map<String, Class<?>> loadExtensionClasses() {
        // 获取 SPI 接口的注解信息
        final SPI defaultAnnotation = type.getAnnotation(SPI.class);
        if (defaultAnnotation != null) {
            String value = defaultAnnotation.value();
            if ((value = value.trim()).length() > 0) {
                // 将 SPI 注解信息的 value 按照 "," 进行切割
                String[] names = NAME_SEPARATOR.split(value);
                if (names.length > 1) {
                    throw new IllegalStateException("默认扩展名超过最大长度[1] " + type.getName() + ": " + Arrays.toString(names));
                }
                if (names.length == 1) {
                    cachedDefaultName = names[0];
                }
            }
        }
        final Map<String, Class<?>> extensionClasses = new HashMap<>(16);
        loadDirectory(extensionClasses, INTERNAL_DIRECTORY);
        loadDirectory(extensionClasses, DIRECTORY);
        loadDirectory(extensionClasses, SERVICES_DIRECTORY);
        return extensionClasses;
    }

    /**
     * 根据 {@link #type} 找到 SPI 目录的描述文件,加载相关的 Class
     *
     * @param extensionClasses Map:{别名 -> Class}
     * @param dir              SPI 文件目录
     */
    private void loadDirectory(final Map<String, Class<?>> extensionClasses, final String dir) {
        // 获取对应的 SPI 接口描述文件
        final String fileName = dir + type.getName();
        try {
            Enumeration<URL> urls;
            final ClassLoader classLoader = findClassLoader();
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
        if (!type.isAssignableFrom(clazz)) {
            throw new IllegalStateException("Error when load extension class(interface: " +
                    type + ", class line: " + clazz.getName() + "), class "
                    + clazz.getName() + "is not subtype of interface.");
        }
        // 判断是否包含 @Adaptive 注解
        if (clazz.isAnnotationPresent(Adaptive.class)) {
            if (cachedAdaptiveClass == null) {
                cachedAdaptiveClass = clazz;
            } else if (!cachedAdaptiveClass.equals(clazz)) {
                throw new IllegalStateException("More than 1 adaptive class found: "
                        + cachedAdaptiveClass.getClass().getName()
                        + ", " + clazz.getClass().getName());
            }
            // 判断是否包含 type 类型的构造函数
        } else if (isWrapperClass(clazz)) {
            Set<Class<?>> wrappers = cachedWrapperClasses;
            if (wrappers == null) {
                cachedWrapperClasses = new ConcurrentHashSet<>();
                wrappers = cachedWrapperClasses;
            }
            wrappers.add(clazz);
        } else {
            // 至少需要一无参构造器
            clazz.getConstructor();
            if (name == null || name.length() == 0) {
                // 默认类名为别名
                name = clazz.getSimpleName();
                if (name.endsWith(type.getSimpleName())) {
                    name = name.substring(0, name.length() - type.getSimpleName().length());
                }
                name = name.toLowerCase();

                if (name.length() == 0) {
                    throw new IllegalStateException("No such extension name for the class " + clazz.getName() + " in the config " + resourceURL);
                }
            }
            final String[] names = NAME_SEPARATOR.split(name);
            if (names != null && names.length > 0) {
                // 获取 @Activate 注解信息
                final Activate activate = clazz.getAnnotation(Activate.class);
                if (activate != null) {
                    // 将第一个别名和注解信息放入 cachedActivates 中.
                    cachedActivates.put(names[0], activate);
                }
                for (String n : names) {
                    if (!cachedNames.containsKey(clazz)) {
                        // 检查 cachedNames 是否包含该 class, 没有则放入 cachedNames 中.
                        cachedNames.put(clazz, n);
                    }
                    Class<?> c = extensionClasses.get(n);
                    if (c == null) {
                        extensionClasses.put(n, clazz);
                    } else if (c != clazz) {
                        throw new IllegalStateException("Duplicate extension " + type.getName() + " name " + n + " on " + c.getName() + " and " + clazz.getName());
                    }
                }
            }
        }
    }

    private T injectExtension(final T instance) {
        try {
            if (objectFactory != null) {
                // 遍历 instance 所有的 public 方法
                for (Method method : instance.getClass().getMethods()) {
                    /**
                     * 条件：
                     * 1) 方法名以 set 开头
                     * 2) 方法只有一个参数
                     * 3) 方法的修饰符为 public
                     */
                    if (method.getName().startsWith("set")
                            && method.getParameterTypes().length == 1
                            && Modifier.isPublic(method.getModifiers())) {
                        Class<?> pt = method.getParameterTypes()[0];
                        try {
                            String property = method.getName().length() > 3 ? method.getName().substring(3, 4).toLowerCase()
                                    + method.getName().substring(4) : "";
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
     * @return 类加载器
     */
    private static ClassLoader findClassLoader() {
        return ExtensionLoader.class.getClassLoader();
    }
}
