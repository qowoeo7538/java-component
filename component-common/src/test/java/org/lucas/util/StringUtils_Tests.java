package org.lucas.util;

import org.junit.Test;
import org.lucas.util.StringUtils;

public class StringUtils_Tests {

    @Test
    public void testCamelToSplitName() {
        System.out.println(StringUtils.camelToSplitName("ExtensionLoader", "."));
    }

}
