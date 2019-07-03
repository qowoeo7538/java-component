package org.lucas.extension;

import org.lucas.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * 通过反射获取{@link #type}的源代码进行动态编译
 */
public class AdaptiveClassCodeGenerator {

    /**
     * 包信息code
     */
    private static final String CODE_PACKAGE = "package %s;\n";

    /**
     * 导入包code
     */
    private static final String CODE_IMPORTS = "import %s;\n";

    /**
     * Class申明信息code.
     */
    private static final String CODE_CLASS_DECLARATION = "public class %s$Adaptive implements %s {\n";

    /**
     * 无适配方法异常代码
     */
    private static final String CODE_UNSUPPORTED = "throw new UnsupportedOperationException(\"The method %s of interface %s is not adaptive method!\");\n";

    private static final String CLASSNAME_INVOCATION = "org.apache.dubbo.rpc.Invocation";

    /**
     *
     */
    private static final String CODE_URL_NULL_CHECK = "if (arg%d == null) throw new IllegalArgumentException(\"url == null\");\n%s url = arg%d;\n";

    /**
     * 扩展名
     */
    private static final String CODE_EXT_NAME_ASSIGNMENT = "String extName = %s;\n";

    /**
     * Class类型
     */
    private final Class<?> type;

    /**
     * 匹配方法参数特殊处理类型
     */
    private final String[] parameterClasses;

    /**
     * 默认扩展名
     */
    private String defaultExtName;

    public AdaptiveClassCodeGenerator(Class<?> type, String[] parameterClasses, String defaultExtName) {
        this.type = type;
        this.parameterClasses = parameterClasses;
        this.defaultExtName = defaultExtName;
    }

    /**
     * @return 动态生成代码
     */
    public String generate() {
        // 不需要生成适配代码,因为不包含 @Adaptive.
        if (!hasAdaptiveMethod()) {
            throw new IllegalStateException("No adaptive method exist on extension " + type.getName() + ", refuse to create the adaptive class!");
        }
        StringBuilder code = new StringBuilder();
        code.append(generatePackageInfo());
        code.append(generateImports());
        code.append(generateClassDeclaration());

        Method[] methods = type.getMethods();
        for (Method method : methods) {
            code.append(generateMethod(method));
        }
        code.append("}");

    }

    /**
     * @return 生成类的包代码.
     */
    private String generatePackageInfo() {
        return String.format(CODE_PACKAGE, type.getPackage().getName());
    }

    /**
     * @return 生成导入信息代码
     */
    private String generateImports() {
        return String.format(CODE_IMPORTS, ExtensionLoader.class.getName());
    }

    /**
     * @return 生成类声明信息代码
     */
    private String generateClassDeclaration() {
        return String.format(CODE_CLASS_DECLARATION, type.getSimpleName(), type.getCanonicalName());
    }

    /**
     * 生成方法代码.
     */
    private String generateMethod(Method method) {
        String methodReturnType = method.getReturnType().getCanonicalName();
        String methodName = method.getName();
        String methodContent = generateMethodContent(method);
        String methodArgs = generateMethodArguments(method);
        // String methodThrows = generateMethodThrows(method);
        // return String.format(CODE_METHOD_DECLARATION, methodReturnType, methodName, methodArgs, methodThrows, methodContent);
    }

    /**
     * 生成方法申明代码
     *
     * @param method {@code Method}
     * @return 方法申明代码
     */
    private String generateMethodContent(Method method) {
        Adaptive adaptiveAnnotation = method.getAnnotation(Adaptive.class);
        StringBuilder code = new StringBuilder(512);
        if (adaptiveAnnotation == null) {
            return generateUnsupported(method);
        } else {
            int urlTypeIndex = getUrlTypeIndex(method);

            // found parameter in URL type
            if (urlTypeIndex != -1) {
                // 空指针异常检查.
                code.append(generateUrlNullCheck(urlTypeIndex));
            } else {
                // 如果没有 ExtURL参数,则寻找包含 ExtURL的 getter 方法.
                code.append(generateUrlAssignmentIndirectly(method));
            }
            boolean hasInvocation = hasInvocationArgument(method);

            String[] value = getMethodAdaptiveValue(adaptiveAnnotation);

            code.append(generateExtNameAssignment(value, hasInvocation));

            // code.append(generateExtNameNullCheck(value));

            // code.append(generateExtensionAssignment());

            // code.append(generateReturnAndInvocation(method));
        }

        return code.toString();
    }

    private String generateUnsupported(Method method) {
        return String.format(CODE_UNSUPPORTED, method, type.getName());
    }

    /**
     * @param method {@code Method}
     * @return {@code ExtURL} 参数在方法的位置
     */
    private int getUrlTypeIndex(Method method) {
        int urlTypeIndex = -1;
        Class<?>[] pts = method.getParameterTypes();
        for (int i = 0; i < pts.length; ++i) {
            if (pts[i].equals(ExtURL.class)) {
                urlTypeIndex = i;
                break;
            }
        }
        return urlTypeIndex;
    }

    /**
     * 判断方法参数是不是有匹配类型
     */
    private boolean hasInvocationArgument(final String[] classType, final Method method) {
        final Class<?>[] pts = method.getParameterTypes();

        return Arrays.stream(pts).anyMatch(p -> IntStream.range(0, classType.length)
                .anyMatch(i -> classType[i].equals(p.getName())));
    }

    /**
     * @return {@code ExtURL} 空异常检查代码.
     */
    private String generateUrlNullCheck(int index) {
        return String.format(CODE_URL_NULL_CHECK, index, ExtURL.class.getName(), index);
    }

    private String generateUrlAssignmentIndirectly(Method method) {
        Class<?>[] pts = method.getParameterTypes();

        for (int i = 0, length = pts.length; i < length; ++i) {
            for (Method m : pts[i].getMethods()) {
                String name = m.getName();
                // 查找 ExtUrl getter方法
                if ((name.startsWith("get") || name.length() > 3)
                        && Modifier.isPublic(m.getModifiers())
                        && !Modifier.isStatic(m.getModifiers())
                        && m.getParameterTypes().length == 0
                        && m.getReturnType() == ExtURL.class) {
                    return generateGetUrlNullCheck(i, pts[i], name);
                }
            }
        }
        // getter 方法没找到,直接抛出异常.
        throw new IllegalStateException("Failed to create adaptive class for interface " + type.getName()
                + ": not found url parameter or url attribute in parameters of method " + method.getName());
    }

    /**
     * 1, test if argi is null
     * 2, test if argi.getXX() returns null
     * 3, assign url with argi.getXX()
     */
    private String generateGetUrlNullCheck(int index, Class<?> type, String method) {
        // 空指针检查
        StringBuilder code = new StringBuilder();
        code.append(String.format("if (arg%d == null) throw new IllegalArgumentException(\"%s argument == null\");\n",
                index, type.getName()));
        code.append(String.format("if (arg%d.%s() == null) throw new IllegalArgumentException(\"%s argument %s() == null\");\n",
                index, method, type.getName(), method));

        code.append(String.format("%s url = arg%d.%s();\n", ExtURL.class.getName(), index, method));
        return code.toString();
    }

    /**
     *
     */
    private String[] getMethodAdaptiveValue(Adaptive adaptiveAnnotation) {
        String[] value = adaptiveAnnotation.value();
        if (value.length == 0) {
            String splitName = StringUtils.camelToSplitName(type.getSimpleName(), ".");
            value = new String[]{splitName};
        }
        return value;
    }

    /**
     * 生成扩展名代码.
     */
    private String generateExtNameAssignment(String[] value, boolean hasInvocation) {
        String getNameCode = null;
        for (int i = value.length - 1; i >= 0; --i) {
            if (i == value.length - 1) {
                if (null != defaultExtName) {
                    if (!"protocol".equals(value[i])) {
                        if (hasInvocation) {
                            getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                        } else {
                            getNameCode = String.format("url.getParameter(\"%s\", \"%s\")", value[i], defaultExtName);
                        }
                    } else {
                        getNameCode = String.format("( url.getProtocol() == null ? \"%s\" : url.getProtocol() )", defaultExtName);
                    }
                } else {
                    if (!"protocol".equals(value[i])) {
                        if (hasInvocation) {
                            getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                        } else {
                            getNameCode = String.format("url.getParameter(\"%s\")", value[i]);
                        }
                    } else {
                        getNameCode = "url.getProtocol()";
                    }
                }
            } else {
                if (!"protocol".equals(value[i])) {
                    if (hasInvocation) {
                        getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                    } else {
                        getNameCode = String.format("url.getParameter(\"%s\", %s)", value[i], getNameCode);
                    }
                } else {
                    getNameCode = String.format("url.getProtocol() == null ? (%s) : url.getProtocol()", getNameCode);
                }
            }
        }

        return String.format(CODE_EXT_NAME_ASSIGNMENT, getNameCode);
    }

    /**
     * @return 类中是否包含适配方法
     */
    private boolean hasAdaptiveMethod() {
        return Arrays.stream(type.getMethods()).anyMatch(m -> m.isAnnotationPresent(Adaptive.class));
    }

}
