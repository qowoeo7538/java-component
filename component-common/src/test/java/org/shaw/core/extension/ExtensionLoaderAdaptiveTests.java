package org.shaw.core.extension;

import org.junit.Test;
import org.shaw.core.extension.adaptive.HasAdaptiveExt;
import org.shaw.core.extension.adaptive.impl.HasAdaptiveExtManualAdaptive;
import org.shaw.core.extension.entity.URL;
import org.shaw.core.extension.ext1.SimpleExt;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @create: 2018-03-01
 * @description:
 */
public class ExtensionLoaderAdaptiveTests {
    @Test
    public void testUseAdaptiveClass() {
        ExtensionLoader<HasAdaptiveExt> loader = ExtensionLoader.getExtensionLoader(HasAdaptiveExt.class);
        HasAdaptiveExt ext = loader.getAdaptiveExtension();
        assertTrue(ext instanceof HasAdaptiveExtManualAdaptive);
    }

    @Test
    public void testGetAdaptiveExtensionDefaultAdaptiveKey() {
        SimpleExt ext = ExtensionLoader.getExtensionLoader(SimpleExt.class).getAdaptiveExtension();
        Map<String, String> map = new HashMap<>();

        URL url = new URL("p1", "1.2.3.4", 1010, "path1", map);
        String echo = ext.echo(url, "haha");
        assertEquals("Ext1Impl1-echo", echo);
    }
}
