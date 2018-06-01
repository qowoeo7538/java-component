package org.shaw.compiler.support;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewConstructor;
import javassist.LoaderClassPath;
import org.shaw.util.ClassUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zx
 * @create: 2018-05-18
 * @description: Javassist动态编译
 */
public class JavassistCompiler extends AbstractCompiler {

    /** 导入包匹配('\n'强制要求必须换行) */
    private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+([\\w\\.\\*]+);\n");

    /** 源代码中父类匹配 */
    private static final Pattern EXTENDS_PATTERN = Pattern.compile("\\s+extends\\s+([\\w\\.]+)[^\\{]*\\{\n");

    /** 源代码中接口匹配 */
    private static final Pattern IMPLEMENTS_PATTERN = Pattern.compile("\\s+implements\\s+([\\w\\.]+)\\s*\\{\n");

    /** 源代码属性匹配 */
    private static final Pattern METHODS_PATTERN = Pattern.compile("\n(private|public|protected)\\s+");

    /** 通过判断 "=" 来确定是否属性. */
    private static final Pattern FIELD_PATTERN = Pattern.compile("[^\n]+=[^\n]+;");

    @Override
    protected Class<?> doCompile(final String name, final String source) throws Throwable {
        int i = name.lastIndexOf('.');
        String className = i < 0 ? name : name.substring(i + 1);
        final ClassPool pool = new ClassPool(true);
        // 添加当前的搜索路径
        pool.appendClassPath(new LoaderClassPath(ClassUtils.getCallerClassLoader(getClass())));
        Matcher matcher = IMPORT_PATTERN.matcher(source);
        List<String> importPackages = new ArrayList<>();
        // 类名 -> 类全名
        Map<String, String> fullNames = new HashMap<>(16);
        while (matcher.find()) {
            // 导入包的全名
            String pkg = matcher.group(1);
            if (pkg.endsWith(".*")) {
                // 导入的包名
                String pkgName = pkg.substring(0, pkg.length() - 2);
                // 添加到 ClassPool 中,在编译的时候搜索.
                pool.importPackage(pkgName);
                importPackages.add(pkgName);
            } else {
                int pi = pkg.lastIndexOf('.');
                if (pi > 0) {
                    // 导入的包名
                    String pkgName = pkg.substring(0, pi);
                    pool.importPackage(pkgName);
                    importPackages.add(pkgName);
                    // 导入的类名 pkg.substring(pi + 1)
                    fullNames.put(pkg.substring(pi + 1), pkg);
                }
            }
        }
        String[] packages = importPackages.toArray(new String[0]);

        matcher = EXTENDS_PATTERN.matcher(source);
        CtClass cls;
        if (matcher.find()) {
            // 继承类
            String extend = matcher.group(1).trim();
            // 继承类全名
            String extendClass;
            if (extend.contains(ClassUtils.PACKAGE_SEPARATOR)) {
                extendClass = extend;
            } else if (fullNames.containsKey(extend)) {
                extendClass = fullNames.get(extend);
            } else {
                extendClass = ClassUtils.forName(packages, extend).getName();
            }
            // 创建一个 public 修饰的类
            cls = pool.makeClass(name, pool.get(extendClass));
        } else {
            cls = pool.makeClass(name);
        }
        matcher = IMPLEMENTS_PATTERN.matcher(source);
        if (matcher.find()) {
            // ifaces: 接口
            String[] ifaces = matcher.group(1).trim().split("\\,");
            for (String iface : ifaces) {
                iface = iface.trim();
                // 接口类全名
                String ifaceClass;
                if (iface.contains(ClassUtils.PACKAGE_SEPARATOR)) {
                    ifaceClass = iface;
                } else if (fullNames.containsKey(iface)) {
                    ifaceClass = fullNames.get(iface);
                } else {
                    ifaceClass = ClassUtils.forName(packages, iface).getName();
                }
                // 将类添加接口
                cls.addInterface(pool.get(ifaceClass));
            }
        }

        // 获取代码体
        String body = source.substring(source.indexOf("{") + 1, source.length() - 1);
        // 根据修饰符对方法进行切割
        String[] methods = METHODS_PATTERN.split(body);
        for (String method : methods) {
            method = method.trim();
            if (method.length() > 0) {
                if (method.startsWith(className)) {
                    // 添加构造方法
                    cls.addConstructor(CtNewConstructor.make("public " + method, cls));
                } else if (FIELD_PATTERN.matcher(method).matches()) {
                    // 添加字段
                    cls.addField(CtField.make("private " + method, cls));
                }
            }
        }
        // 通过类加载器,和权限域创建对象.
        return cls.toClass(ClassUtils.getCallerClassLoader(getClass()), JavassistCompiler.class.getProtectionDomain());
    }
}
