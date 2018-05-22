package org.shaw.compiler.support;

import javassist.ClassPool;
import javassist.LoaderClassPath;
import org.shaw.util.ClassHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @create: 2018-05-18
 * @description: Javassist动态编译
 */
public class JavassistCompiler extends AbstractCompiler {

    /** 导入包匹配('\n'强制要求必须换行) */
    private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+([\\w\\.\\*]+);\n");

    /** 源代码中父类匹配 */
    private static final Pattern EXTENDS_PATTERN = Pattern.compile("\\s+extends\\s+([\\w\\.]+)[^\\{]*\\{\n");

    @Override
    protected Class<?> doCompile(final String name, final String source) throws Throwable {
        int i = name.lastIndexOf('.');
        String className = i < 0 ? name : name.substring(i + 1);
        final ClassPool pool = new ClassPool(true);
        // 添加当前的搜索路径
        pool.appendClassPath(new LoaderClassPath(ClassHelper.getCallerClassLoader(getClass())));
        Matcher matcher = IMPORT_PATTERN.matcher(source);
        List<String> importPackages = new ArrayList<>();
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
        if (matcher.find()) {
            // 继承类
            String extend = matcher.group(1).trim();
            // 继承类全名
            String extendClass;
            if (extend.contains(ClassHelper.PACKAGE_SEPARATOR)) {
                extendClass = extend;
            } else if (fullNames.containsKey(extend)) {
                extendClass = fullNames.get(extend);
            } else {
                extendClass = ClassHelper.forName(packages, extend).getName();
            }
        }
        return null;
    }
}
