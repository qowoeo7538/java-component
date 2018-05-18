package org.shaw.compiler.support;

import javassist.ClassPool;
import javassist.LoaderClassPath;
import org.shaw.util.ClassHelper;

/**
 * @create: 2018-05-18
 * @description:
 */
public class JavassistCompiler extends AbstractCompiler {
    @Override
    protected Class<?> doCompile(final String name, final String source) throws Throwable {
        int i = name.lastIndexOf('.');
        String className = i < 0 ? name : name.substring(i + 1);
        ClassPool pool = new ClassPool(true);

        pool.appendClassPath(new LoaderClassPath(ClassHelper.getCallerClassLoader(getClass())));
        return null;
    }
}
