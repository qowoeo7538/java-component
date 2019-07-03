package org.lucas.component.common.util;

import org.junit.Test;
import org.lucas.component.common.util.StringUtils;

public class StringUtils_Tests {

    @Test
    public void testCamelToSplitName() {
        System.out.println(StringUtils.camelToSplitName("ExtensionLoader", "."));
    }

}
