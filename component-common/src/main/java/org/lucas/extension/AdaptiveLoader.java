package org.lucas.extension;

import org.lucas.component.common.compiler.Compiler;
import org.lucas.util.ClassUtils;

import java.lang.annotation.Annotation;

/**
 * 通过动态编译进行适配加载
 */
public abstract class AdaptiveLoader extends AbstractLoader {

    /**
     * 适配的类注解
     */
    private final Class<? extends Annotation> classAnnotation;

    /**
     * 适配的方法注解
     */
    private final Class<? extends Annotation> methodAnnotation;

    protected AdaptiveLoader(Class<? extends Annotation> classAnnotation, Class<? extends Annotation> methodAnnotation) {
        this.classAnnotation = classAnnotation;
        this.methodAnnotation = methodAnnotation;
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
        ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
        Compiler compiler = ExtensionLoader.getExtensionLoader(Compiler.class)
                .getAdaptiveExtension();
        return compiler.compile(code, classLoader);
    }


}
