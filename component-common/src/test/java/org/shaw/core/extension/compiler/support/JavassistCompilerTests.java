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
        final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+([\\w\\.\\*]+);\n");
        String str = "package org.shaw.core.extension.compiler.support;\n"
                + "import org.junit.Test;\n"
                + "import java.util.regex.Matcher;\n"
                + "import java.util.regex.Pattern;";

    }

}
