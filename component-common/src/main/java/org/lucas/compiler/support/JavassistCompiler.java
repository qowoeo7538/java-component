package org.lucas.compiler.support;

import javassist.CtClass;
import org.lucas.util.ClassUtils;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zx
 * @create: 2018-05-18
 * @description: Javassist动态编译
 */
public class JavassistCompiler extends AbstractCompiler {

    /**
     * 导入包匹配('\n'强制要求必须换行)
     */
    private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+([\\w\\.\\*]+);\n");

    /**
     * 源代码中父类匹配
     */
    private static final Pattern EXTENDS_PATTERN = Pattern.compile("\\s+extends\\s+([\\w\\.]+)[^\\{]*\\{\n");

    /**
     * 源代码中接口匹配
     */
    private static final Pattern IMPLEMENTS_PATTERN = Pattern.compile("\\s+implements\\s+([\\w\\.]+)\\s*\\{\n");

    /**
     * 源代码属性匹配
     */
    private static final Pattern METHODS_PATTERN = Pattern.compile("\n(private|public|protected)\\s+");

    /**
     * 通过判断 "=" 来确定是否属性.
     */
    private static final Pattern FIELD_PATTERN = Pattern.compile("[^\n]+=[^\n]+;");

    @Override
    protected Class<?> doCompile(final String name, final String source) throws Throwable {
        final CtClassBuilder builder = new CtClassBuilder();
        builder.setClassName(name);

        // 处理导入类信息
        Matcher matcher = IMPORT_PATTERN.matcher(source);
        while (matcher.find()) {
            builder.addImports(matcher.group(1).trim());
        }

        // 处理扩展父类信息
        matcher = EXTENDS_PATTERN.matcher(source);
        if (matcher.find()) {
            builder.setSuperClassName(matcher.group(1).trim());
        }

        // 处理实现接口信息
        matcher = IMPLEMENTS_PATTERN.matcher(source);
        if (matcher.find()) {
            String[] ifaces = matcher.group(1).trim().split("\\,");
            Arrays.stream(ifaces).forEach(i -> builder.addInterface(i.trim()));
        }

        // 处理构造函数,属性字段,方法信息.
        String body = source.substring(source.indexOf('{') + 1, source.length() - 1);
        String[] methods = METHODS_PATTERN.split(body);
        String className = ClassUtils.getShortName(name);
        Arrays.stream(methods).map(String::trim).filter(m -> !m.isEmpty()).forEach(method -> {
            if (method.startsWith(className)) {
                builder.addConstructor("public " + method);
            } else if (FIELD_PATTERN.matcher(method).matches()) {
                builder.addField("private " + method);
            } else {
                builder.addMethod("public " + method);
            }
        });

        // 获取类加载器
        ClassLoader classLoader = ClassUtils.getCallerClassLoader(getClass());
        CtClass cls = builder.build(classLoader);
        // 通过类加载器,和权限域创建对象.
        return cls.toClass(ClassUtils.getCallerClassLoader(getClass()), JavassistCompiler.class.getProtectionDomain());
    }
}



