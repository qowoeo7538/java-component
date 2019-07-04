package org.lucas.component.common.compiler.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AdaptiveCompilerTest extends JavaCodeTest {

    @Test
    public void testAvailableCompiler() throws Exception {
        AdaptiveCompiler.setDefaultCompiler("jdk");
        AdaptiveCompiler compiler = new AdaptiveCompiler();
        Class<?> clazz = compiler.compile(getSimpleCode(), AdaptiveCompiler.class.getClassLoader());
        HelloService helloService = (HelloService) clazz.getConstructor().newInstance();
        Assertions.assertEquals("Hello world!", helloService.sayHello());
    }

}
