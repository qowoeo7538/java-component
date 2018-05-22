package org.shaw.core.extension.compiler.support;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @create: 2018-05-18
 * @description:
 */
public class JavassistCompilerTests {

    @Test
    public void testJavassistCompiler() {
        final Pattern EXTENDS_PATTERN = Pattern.compile("\\s+extends\\s+([\\w\\.]+)[^\\{]*\\{\n");
        String source = "public class JavassistCompiler extends AbstractCompiler {\n"
                + "}";
        Matcher matcher = EXTENDS_PATTERN.matcher(source);
        if (matcher.find()) {
            System.out.println(matcher.group());
            System.out.println(matcher.group(1));
        }
    }

}
