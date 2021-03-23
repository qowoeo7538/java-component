package org.lucas.component.common.util;

import org.junit.jupiter.api.Test;

class StringUtils_Tests {

    @Test
    void testCamelToSplitName() {
        System.out.println(StringHelper.camelToSplitName("ExtensionLoader", "."));
    }

}
