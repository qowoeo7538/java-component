package org.lucas.component.common.extension;

import org.junit.Assert;
import org.junit.Test;

import org.lucas.component.common.extension.ext1.SimpleExt;
import org.lucas.component.common.extension.ext2.Ext2;
import org.lucas.component.common.extension.ext2.UrlHolder;
import org.lucas.component.common.extension.ext3.UseProtocolKeyExt;
import org.lucas.component.common.extension.ext4.NoUrlParamExt;
import org.lucas.component.common.extension.adaptive.HasAdaptiveExt;
import org.lucas.component.common.extension.adaptive.impl.HasAdaptiveExtManualAdaptive;
import org.lucas.component.common.extension.ext6_inject.Ext6;
import org.lucas.component.common.extension.ext6_inject.impl.Ext6Impl2;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * SPI 测试
 */
public class ExtensionLoader_Adaptive_Test {

    @Test
    public void test_useAdaptiveClass() {
        ExtensionLoader<HasAdaptiveExt> loader = ExtensionLoader.getExtensionLoader(HasAdaptiveExt.class);
        HasAdaptiveExt ext = loader.getAdaptiveExtension();
        assertTrue(ext instanceof HasAdaptiveExtManualAdaptive);
    }

    @Test
    public void test_getAdaptiveExtension_UrlNpe() throws Exception {
        SimpleExt ext = ExtensionLoader.getExtensionLoader(SimpleExt.class).getAdaptiveExtension();

        try {
            ext.echo(null, "haha");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("url == null", e.getMessage());
        }
    }

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

    @Test
    public void test_getAdaptiveExtension_InjectNotExtFail() throws Exception {
        Ext6 ext = ExtensionLoader.getExtensionLoader(Ext6.class).getExtension("impl2");

        Ext6Impl2 impl = (Ext6Impl2) ext;
        assertNull(impl.getList());
    }
}
