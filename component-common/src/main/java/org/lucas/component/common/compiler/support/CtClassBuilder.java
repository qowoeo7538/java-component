package org.lucas.component.common.compiler.support;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import org.lucas.component.common.util.ClassUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: shaw
 * @Date: 2019/5/17 16:40
 */
public class CtClassBuilder {

    /**
     * 类名
     */
    private String className;

    /**
     * 所继承的父类,默认为 java.lang.Object.
     */
    private String superClassName = "java.lang.Object";

    /**
     * 类包
     */
    private List<String> imports = new ArrayList<>();

    /**
     * 类短名 --> 类全名
     */
    private Map<String, String> fullNames = new HashMap<>();

    /**
     * 接口类
     */
    private List<String> ifaces = new ArrayList<>();

    /**
     * 构造方法
     */
    private List<String> constructors = new ArrayList<>();

    /**
     * 字段属性
     */
    private List<String> fields = new ArrayList<>();

    /**
     * 方法
     */
    private List<String> methods = new ArrayList<>();

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSuperClassName() {
        return superClassName;
    }

    public void setSuperClassName(String superClassName) {
        this.superClassName = getQualifiedClassName(superClassName);
    }

    public void addInterface(String iface) {
        this.ifaces.add(getQualifiedClassName(iface));
    }

    public List<String> getInterfaces() {
        return ifaces;
    }

    public List<String> getConstructors() {
        return constructors;
    }

    public void addConstructor(String constructor) {
        this.constructors.add(constructor);
    }

    public List<String> getFields() {
        return fields;
    }

    public void addField(String field) {
        this.fields.add(field);
    }

    public List<String> getMethods() {
        return methods;
    }

    public void addMethod(String method) {
        this.methods.add(method);
    }

    /**
     * 添加引入类的信息.
     *
     * @param pkg 包信息
     */
    public void addImports(String pkg) {
        int pi = pkg.lastIndexOf('.');
        if (pi > 0) {
            String pkgName = pkg.substring(0, pi);
            this.imports.add(pkgName);
            if (!pkg.endsWith(".*")) {
                fullNames.put(pkg.substring(pi + 1), pkg);
            }
        }
    }

    public CtClass build(ClassLoader classLoader) throws NotFoundException, CannotCompileException {
        ClassPool pool = new ClassPool(true);
        pool.appendClassPath(new LoaderClassPath(classLoader));

        // 创建类及父类
        CtClass ctClass = pool.makeClass(className, pool.get(superClassName));

        // 添加导入包
        imports.forEach(pool::importPackage);

        // 添加实现接口
        for (String iface : ifaces) {
            ctClass.addInterface(pool.get(iface));
        }

        // 添加构造方法
        for (String constructor : constructors) {
            ctClass.addConstructor(CtNewConstructor.make(constructor, ctClass));
        }

        // 添加属性
        for (String field : fields) {
            ctClass.addField(CtField.make(field, ctClass));
        }

        // 添加方法
        for (String method : methods) {
            ctClass.addMethod(CtNewMethod.make(method, ctClass));
        }
        return ctClass;
    }

    /**
     * 根据类名,获取当前 CtClassBuilder 中包含的类全名
     *
     * @param className 类名
     * @return 类全名
     */
    protected String getQualifiedClassName(String className) {
        if (className.contains(".")) {
            return className;
        }
        if (fullNames.containsKey(className)) {
            return fullNames.get(className);
        }
        return ClassUtils.forName(imports.toArray(new String[0]), className).getName();
    }
}
