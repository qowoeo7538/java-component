package org.lucas.component.common.extension;

import org.junit.Assert;
import org.junit.Test;
import org.lucas.component.common.extension.ext10_abstract.SimpleExt10;
import org.lucas.component.common.extension.ext9_empty.Ext9Empty;
import org.lucas.component.common.extension.ext9_empty.impl.Ext9EmptyImpl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ExtensionLoader_Test {

    @Test
    public void test_AddExtension_NoExtend() throws Exception {
        ExtensionLoader.getExtensionLoader(Ext9Empty.class).getSupportedExtensions();
        ExtensionLoader.getExtensionLoader(Ext9Empty.class).addExtension("ext9", Ext9EmptyImpl.class);
        Ext9Empty ext = ExtensionLoader.getExtensionLoader(Ext9Empty.class).getExtension("ext9");

        assertThat(ext, instanceOf(Ext9Empty.class));
        assertEquals("ext9", ExtensionLoader.getExtensionLoader(Ext9Empty.class).getExtensionName(Ext9EmptyImpl.class));
    }

    @Test
    public void test_getAbstractExtension() throws Exception {
        SimpleExt10 ext = ExtensionLoader.getExtensionLoader(SimpleExt10.class).getDefaultExtension();
        assertThat(ext, instanceOf(SimpleExt10.class));
        Assert.assertEquals("abstract-print\nimpl1-call", ext.print());

        String name = ExtensionLoader.getExtensionLoader(SimpleExt10.class).getDefaultExtensionName();
        assertEquals("impl1", name);
    }

}