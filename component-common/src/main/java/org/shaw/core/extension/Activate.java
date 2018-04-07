package org.shaw.core.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @create: 2018-03-06
 * @description:
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Activate {

    String[] value() default {};

    String[] group() default {};

    String[] before() default {};

    String[] after() default {};

    int order() default 0;
}
