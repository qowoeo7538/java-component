package org.lucas.component.common.extension;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.lucas.component.common.core.Constants;
import org.lucas.component.common.core.collect.ConcurrentHashSet;
import org.lucas.component.common.extension.support.ActivateComparator;
import org.lucas.component.common.util.ClassUtils;
import org.lucas.component.common.util.ConfigUtils;
import org.lucas.component.common.util.ExceptionUtils;
import org.lucas.component.common.util.Holder;
import org.lucas.component.common.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ExtensionLoader<T> {

    private static final String REMOVE_VALUE_PREFIX = "-";

    private static final String DEFAULT_KEY = "default";

    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();

    /**
     * {实现类 Class -> Class 对象的实例}
     */
    private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    // =============================

    /**
     * Map:{别名 -> 类的 @Activate 信息}
     */
    private final Map<String, Object> cachedActivates = new ConcurrentHashMap<>();

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

    public List<T> getActivateExtension(final ExtURL url, final String key, final String group) {
        final String value = url.getParameter(key);
        return getActivateExtension(url, value == null || value.length() == 0 ? null : Constants.COMMA_SPLIT_PATTERN.split(value), group);
    }

    public List<T> getActivateExtension(final ExtURL url, final String[] values, final String group) {
        final MutableList<T> exts = Lists.mutable.empty();
        final List<String> names = values == null ? new ArrayList<>(0) : Arrays.asList(values);

        if (!names.contains(REMOVE_VALUE_PREFIX + DEFAULT_KEY)) {
            getExtensionClasses();
            for (Iterator<Map.Entry<String, Object>> iterator = cachedActivates.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, Object> entry = iterator.next();
                String name = entry.getKey();
                Object activate = entry.getValue();

                String[] activateGroup, activateValue;

                if (activate instanceof Activate) {
                    activateGroup = ((Activate) activate).group();
                    activateValue = ((Activate) activate).value();
                } else {
                    continue;
                }

                if (isMatchGroup(group, activateGroup)) {
                    T ext = getExtension(name);
                    if (!names.contains(name)
                            && !names.contains(REMOVE_VALUE_PREFIX + name)
                            && isActive(activateValue, url)) {
                        exts.add(ext);
                    }
                }
            }
            exts.sort(ActivateComparator.getComparator());
        }
        List<T> usrs = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            if (!name.startsWith(REMOVE_VALUE_PREFIX)
                    && !names.contains(REMOVE_VALUE_PREFIX + name)) {
                if (DEFAULT_KEY.equals(name)) {
                    if (!usrs.isEmpty()) {
                        exts.addAll(0, usrs);
                        usrs.clear();
                    }
                } else {
                    T ext = getExtension(name);
                    usrs.add(ext);
                }
            }
        }
        if (!usrs.isEmpty()) {
            exts.addAll(usrs);
        }
        return exts;
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
     * @return 默认的别名, 如果返回 {@code null} 则没有配置
     */
    public String getDefaultExtensionName() {
        getExtensionClasses();
        return cachedDefaultName;
    }

    public boolean hasExtension(String name) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Extension name == null");
        }
        try {
            this.getExtensionClass(name);
            return true;
        } catch (Throwable t) {
            return false;
        }
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


    private boolean isActive(final String[] keys, final ExtURL url) {
        if (keys.length == 0) {
            return true;
        }
        for (String key : keys) {
            for (Iterator<Map.Entry<String, String>> iterator = url.getParameters().entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, String> entry = iterator.next();
                String k = entry.getKey();
                String v = entry.getValue();
                if ((k.equals(key) || k.endsWith("." + key))
                        && !ConfigUtils.isEmpty(v)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断一个组是否包含在一组列表中
     *
     * @param group  组名
     * @param groups 数组列表
     * @return if {@code true} 包含
     */
    private boolean isMatchGroup(final String group, final String[] groups) {
        if (group == null || group.length() == 0) {
            return true;
        }
        if (groups != null && groups.length > 0) {
            for (String g : groups) {
                if (group.equals(g)) {
                    return true;
                }
            }
        }
        return false;
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
         * import            : org.lucas.core.extension.ExtensionLoader
         * class 描述        : public class {@code type.getSimpleName()}$Adaptive implements {@code type.getCanonicalName()}
         */
        codeBuilder.append("package " + type.getPackage().getName() + ";");
        codeBuilder.append("\nimport " + ExtensionLoader.class.getName() + ";");
        codeBuilder.append("\npublic class " + type.getSimpleName() + "$Adaptive" + " implements "
                + type.getCanonicalName() + " {");
        // private AtomicInteger count = new AtomicInteger(0);
        codeBuilder.append("\nprivate java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger(0);\n");

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
                // ExtRUL 处理
                int urlTypeIndex = -1;
                for (int i = 0; i < pts.length; ++i) {
                    if (pts[i].equals(ExtURL.class)) {
                        urlTypeIndex = i;
                        break;
                    }
                }

                // 对 ExtURL 参数进行空指针检查
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
                                    && m.getReturnType() == ExtURL.class) {
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
                    s = String.format("%s url = arg%d.%s();", ExtURL.class.getName(), urlTypeIndex, attribMethod);
                    code.append(s);
                }

                String[] value = adaptiveAnnotation.value();
                // 默认 value
                if (value.length == 0) {
                    String splitName = StringUtils.camelToSplitName(type.getSimpleName(), ".");
                    value = new String[]{splitName};
                }

                String defaultExtName = cachedDefaultName;
                String getNameCode = null;
                for (int i = value.length - 1; i >= 0; --i) {
                    if (i == value.length - 1) {
                        if (null != defaultExtName) {
                            // 判断注解中是否存在 "protocol"
                            if (!"protocol".equals(value[i])) {
                                getNameCode = String.format("url.getParameter(\"%s\", \"%s\")", value[i], defaultExtName);
                            } else {
                                getNameCode = String.format("( url.getProtocol() == null ? \"%s\" : url.getProtocol() )", defaultExtName);
                            }
                        } else {
                            if (!"protocol".equals(value[i])) {
                                getNameCode = String.format("url.getParameter(\"%s\")", value[i]);
                            } else {
                                getNameCode = "url.getProtocol()";
                            }
                        }
                    } else {
                        if (!"protocol".equals(value[i])) {
                            getNameCode = String.format("url.getParameter(\"%s\", %s)", value[i], getNameCode);
                        } else {
                            getNameCode = String.format("url.getProtocol() == null ? (%s) : url.getProtocol()", getNameCode);
                        }
                    }
                }

                code.append("\nString extName = ").append(getNameCode).append(";");
                String s = String.format("\nif(extName == null) " +
                                "throw new IllegalStateException(\"Fail to get extension(%s) name from url(\" + url.toString() + \") use keys(%s)\");",
                        type.getName(), Arrays.toString(value));
                code.append(s);

                code.append(String.format("\n%s extension = null;\n try {\nextension = (%<s)%s.getExtensionLoader(%s.class).getExtension(extName);\n}catch(Exception e){\n",
                        type.getName(), ExtensionLoader.class.getSimpleName(), type.getName()));
                code.append(String.format("extension = (%s)%s.getExtensionLoader(%s.class).getExtension(\"%s\");\n}",
                        type.getName(), ExtensionLoader.class.getSimpleName(), type.getName(), defaultExtName));

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
            throw new IllegalArgumentException("Extension name == null");
        }
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new IllegalStateException("No such extension \"" + name + "\" for " + type.getName() + "!");
        }
        return clazz;
    }


    /**
     * 读取的 SPI 目录下对应的文件,加载 Class.
     *
     * @return Map:{别名 -> Class}
     * @see #loadDirectory(Map, String, String)
     */
    private Map<String, Class<?>> loadExtensionClasses() {
        // 获取 SPI 接口的注解信息
        final SPI defaultAnnotation = type.getAnnotation(SPI.class);
        if (defaultAnnotation != null) {
            String value = defaultAnnotation.value();
            if ((value = value.trim()).length() > 0) {
                // 将 SPI 注解信息的 value 按照 "," 进行切割
                String[] names = Constants.COMMA_SPLIT_PATTERN.split(value);
                if (names.length > 1) {
                    throw new IllegalStateException("默认扩展名超过最大长度[1] " + type.getName() + ": " + Arrays.toString(names));
                }
                if (names.length == 1) {
                    cachedDefaultName = names[0];
                }
            }
        }
        final Map<String, Class<?>> extensionClasses = new HashMap<>(16);
        loadDirectory(extensionClasses, INTERNAL_DIRECTORY, type.getName());
        loadDirectory(extensionClasses, DIRECTORY, type.getName());
        loadDirectory(extensionClasses, SERVICES_DIRECTORY, type.getName());
        return extensionClasses;
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
            // 至少需要一个无参构造器
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
            final String[] names = Constants.COMMA_SPLIT_PATTERN.split(name);
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
                    /**
                     * 条件：
                     * 1) 方法名以 set 开头
                     * 2) 方法只有一个参数
                     * 3) 方法的修饰符为 public
                     */
                    if (method.getName().startsWith("set")
                            && method.getParameterTypes().length == 1
                            && Modifier.isPublic(method.getModifiers())) {
                        if (method.getAnnotation(DisableInject.class) != null) {
                            continue;
                        }
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
}
