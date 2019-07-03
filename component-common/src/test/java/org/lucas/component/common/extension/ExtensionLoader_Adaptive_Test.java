package org.lucas.component.common.extension;

import org.junit.Test;
import org.lucas.component.common.extension.ext4.NoUrlParamExt;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * SPI 测试
 */
public class ExtensionLoader_Adaptive_Test {


    @Test
    public void test_getAdaptiveExtension_ExceptionWhenNoUrlAttribute() throws Exception {
        try {
            ExtensionLoader.getExtensionLoader(NoUrlParamExt.class).getAdaptiveExtension();
            fail();
        } catch (Exception expected) {
            assertThat(expected.getMessage(), containsString("fail to create adaptive class for interface "));
            assertThat(expected.getMessage(), containsString(": not found url parameter or url attribute in parameters of method "));
        }
    }

}
