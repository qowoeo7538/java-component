package org.lucas.component.common.util;

import org.junit.jupiter.api.Test;

public class StringUtils_Tests {

    @Test
    public void testCamelToSplitName() {
        System.out.println(StringHelper.camelToSplitName("ExtensionLoader", "."));
    }

}
