package org.lucas.component.common.extension;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.lucas.component.common.extension.ext10_abstract.SimpleExt10;
import org.lucas.component.common.extension.ext9_empty.Ext9Empty;
import org.lucas.component.common.extension.ext9_empty.impl.Ext9EmptyImpl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


class ExtensionLoader_Test {

    @Test
    void test_AddExtension_NoExtend() throws Exception {
        ExtensionLoader.getExtensionLoader(Ext9Empty.class).getSupportedExtensions();
        ExtensionLoader.getExtensionLoader(Ext9Empty.class).addExtension("ext9", Ext9EmptyImpl.class);
        Ext9Empty ext = ExtensionLoader.getExtensionLoader(Ext9Empty.class).getExtension("ext9");

        assertThat(ext, instanceOf(Ext9Empty.class));
        assertEquals("ext9", ExtensionLoader.getExtensionLoader(Ext9Empty.class).getExtensionName(Ext9EmptyImpl.class));
    }

    @Test
    void test_getAbstractExtension() throws Exception {
        ExtensionLoader<SimpleExt10> loader = ExtensionLoader.getExtensionLoader(SimpleExt10.class);
        SimpleExt10 ext = loader.getDefaultExtension();
        assertThat(ext, instanceOf(SimpleExt10.class));
        assertEquals("abstract-print\nimpl1-call", ext.print());

        String name = ExtensionLoader.getExtensionLoader(SimpleExt10.class).getDefaultExtensionName();
        assertEquals("impl1", name);
    }

    @Test
    void test_hasExtension() {
        ExtensionLoader<SimpleExt10> loader = ExtensionLoader.getExtensionLoader(SimpleExt10.class);
        boolean isExist = loader.hasExtension("impl1");
        Assertions.assertTrue(isExist);
        isExist = loader.hasExtension("impl10");
        Assertions.assertFalse(isExist);
    }

}