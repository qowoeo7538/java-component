package org.lucas.extension;

import org.junit.Assert;
import org.junit.Test;
import org.lucas.core.Constants;
import org.lucas.extension.activate.ActivateExt1;
import org.lucas.extension.activate.impl.ActivateExt1Impl1;
import org.lucas.extension.activate.impl.GroupActivateExtImpl;
import org.lucas.extension.activate.impl.OrderActivateExtImpl1;
import org.lucas.extension.activate.impl.OrderActivateExtImpl2;
import org.lucas.extension.activate.impl.ValueActivateExtImpl;
import org.lucas.extension.ext6_wrap.WrappedExt;
import org.lucas.extension.ext8_add.AddExt1;
import org.lucas.extension.ext8_add.AddExt2;
import org.lucas.extension.ext8_add.impl.AddExt1_ManualAdd1;
import org.lucas.extension.ext8_add.impl.AddExt2_ManualAdaptive;
import org.lucas.extension.ext1.SimpleExt;
import org.lucas.extension.ext1.impl.SimpleExtImpl1;
import org.lucas.extension.ext1.impl.SimpleExtImpl2;
import org.lucas.extension.ext10_abstract.SimpleExt10;
import org.lucas.extension.ext2.Ext2;
import org.lucas.extension.ext6_wrap.impl.Ext5Wrapper1;
import org.lucas.extension.ext6_wrap.impl.Ext5Wrapper2;
import org.lucas.extension.ext9_empty.Ext9Empty;
import org.lucas.extension.ext9_empty.impl.Ext9EmptyImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ExtensionLoader_Test {

    @Test
    public void test_getDefaultExtension() throws Exception {

        SimpleExt ext = ExtensionLoader.getExtensionLoader(SimpleExt.class).getDefaultExtension();
        assertThat(ext, instanceOf(SimpleExtImpl1.class));

        String name = ExtensionLoader.getExtensionLoader(SimpleExt.class).getDefaultExtensionName();
        assertEquals("impl1", name);
    }

    @Test
    public void test_getDefaultExtension_NULL() throws Exception {
        Ext2 ext = ExtensionLoader.getExtensionLoader(Ext2.class).getDefaultExtension();
        assertNull(ext);

        String name = ExtensionLoader.getExtensionLoader(Ext2.class).getDefaultExtensionName();
        assertNull(name);
    }

    @Test
    public void test_getExtension() throws Exception {
        assertTrue(ExtensionLoader.getExtensionLoader(SimpleExt.class).getExtension("impl1") instanceof SimpleExtImpl1);
        assertTrue(ExtensionLoader.getExtensionLoader(SimpleExt.class).getExtension("impl2") instanceof SimpleExtImpl2);
    }

    @Test
    public void test_getExtension_WithWrapper() throws Exception {
        WrappedExt impl1 = ExtensionLoader.getExtensionLoader(WrappedExt.class).getExtension("impl1");
        assertThat(impl1, anyOf(instanceOf(Ext5Wrapper1.class), instanceOf(Ext5Wrapper2.class)));

        WrappedExt impl2 = ExtensionLoader.getExtensionLoader(WrappedExt.class).getExtension("impl2");
        assertThat(impl2, anyOf(instanceOf(Ext5Wrapper1.class), instanceOf(Ext5Wrapper2.class)));


        ExtURL url = new ExtURL("p1", "1.2.3.4", 1010, "path1");
        int echoCount1 = Ext5Wrapper1.echoCount.get();
        int echoCount2 = Ext5Wrapper2.echoCount.get();

        Assert.assertEquals("Ext5Impl1-echo", impl1.echo(url, "ha"));
        assertEquals(echoCount1 + 1, Ext5Wrapper1.echoCount.get());
        assertEquals(echoCount2 + 1, Ext5Wrapper2.echoCount.get());
    }

    @Test
    public void test_hasExtension() throws Exception {
        assertTrue(ExtensionLoader.getExtensionLoader(SimpleExt.class).hasExtension("impl1"));
        assertFalse(ExtensionLoader.getExtensionLoader(SimpleExt.class).hasExtension("impl1,impl2"));
        assertFalse(ExtensionLoader.getExtensionLoader(SimpleExt.class).hasExtension("xxx"));
    }

    @Test
    public void test_hasExtension_wrapperIsNotExt() throws Exception {
        assertTrue(ExtensionLoader.getExtensionLoader(WrappedExt.class).hasExtension("impl1"));
        assertFalse(ExtensionLoader.getExtensionLoader(WrappedExt.class).hasExtension("impl1,impl2"));
        assertFalse(ExtensionLoader.getExtensionLoader(WrappedExt.class).hasExtension("xxx"));

        assertFalse(ExtensionLoader.getExtensionLoader(WrappedExt.class).hasExtension("wrapper1"));
    }

    @Test
    public void test_getSupportedExtensions() throws Exception {
        Set<String> exts = ExtensionLoader.getExtensionLoader(SimpleExt.class).getSupportedExtensions();

        Set<String> expected = new HashSet<String>();
        expected.add("impl1");
        expected.add("impl2");
        expected.add("impl3");

        assertEquals(expected, exts);
    }

    @Test
    public void test_getSupportedExtensions_wrapperIsNotExt() throws Exception {
        Set<String> exts = ExtensionLoader.getExtensionLoader(WrappedExt.class).getSupportedExtensions();

        Set<String> expected = new HashSet<String>();
        expected.add("impl1");
        expected.add("impl2");

        assertEquals(expected, exts);
    }

    @Test
    public void test_AddExtension() throws Exception {
        ExtensionLoader.getExtensionLoader(AddExt1.class).addExtension("Manual1", AddExt1_ManualAdd1.class);
        AddExt1 ext = ExtensionLoader.getExtensionLoader(AddExt1.class).getExtension("Manual1");

        assertThat(ext, instanceOf(AddExt1_ManualAdd1.class));
        assertEquals("Manual1", ExtensionLoader.getExtensionLoader(AddExt1.class).getExtensionName(AddExt1_ManualAdd1.class));
    }

    @Test
    public void test_AddExtension_NoExtend() throws Exception {
//        ExtensionLoader.getExtensionLoader(Ext9Empty.class).getSupportedExtensions();
        ExtensionLoader.getExtensionLoader(Ext9Empty.class).addExtension("ext9", Ext9EmptyImpl.class);
        Ext9Empty ext = ExtensionLoader.getExtensionLoader(Ext9Empty.class).getExtension("ext9");

        assertThat(ext, instanceOf(Ext9Empty.class));
        assertEquals("ext9", ExtensionLoader.getExtensionLoader(Ext9Empty.class).getExtensionName(Ext9EmptyImpl.class));
    }

    @Test
    public void test_AddExtension_Adaptive() throws Exception {
        ExtensionLoader<AddExt2> loader = ExtensionLoader.getExtensionLoader(AddExt2.class);
        loader.addExtension(null, AddExt2_ManualAdaptive.class);

        AddExt2 adaptive = loader.getAdaptiveExtension();
        assertTrue(adaptive instanceof AddExt2_ManualAdaptive);
    }

    @Test
    public void testLoadActivateExtension() throws Exception {
        // test default
        ExtURL url = ExtURL.valueOf("test://localhost/test");
        List<ActivateExt1> list = ExtensionLoader.getExtensionLoader(ActivateExt1.class)
                .getActivateExtension(url, new String[]{}, "default_group");
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.get(0).getClass() == ActivateExt1Impl1.class);

        // test group
        url = url.addParameter(Constants.GROUP_KEY, "group1");
        list = ExtensionLoader.getExtensionLoader(ActivateExt1.class)
                .getActivateExtension(url, new String[]{}, "group1");
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.get(0).getClass() == GroupActivateExtImpl.class);

        // test value
        url = url.removeParameter(Constants.GROUP_KEY);
        url = url.addParameter(Constants.GROUP_KEY, "value");
        url = url.addParameter("value", "value");
        list = ExtensionLoader.getExtensionLoader(ActivateExt1.class)
                .getActivateExtension(url, new String[]{}, "value");
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.get(0).getClass() == ValueActivateExtImpl.class);

        // test order
        url = ExtURL.valueOf("test://localhost/test");
        url = url.addParameter(Constants.GROUP_KEY, "order");
        list = ExtensionLoader.getExtensionLoader(ActivateExt1.class)
                .getActivateExtension(url, new String[]{}, "order");
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.get(0).getClass() == OrderActivateExtImpl1.class);
        Assert.assertTrue(list.get(1).getClass() == OrderActivateExtImpl2.class);
    }

    @Test
    public void testLoadDefaultActivateExtension() throws Exception {
        // test default
        ExtURL url = ExtURL.valueOf("test://localhost/test?ext=order1,default");
        List<ActivateExt1> list = ExtensionLoader.getExtensionLoader(ActivateExt1.class)
                .getActivateExtension(url, "ext", "default_group");
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.get(0).getClass() == OrderActivateExtImpl1.class);
        Assert.assertTrue(list.get(1).getClass() == ActivateExt1Impl1.class);

        url = ExtURL.valueOf("test://localhost/test?ext=default,order1");
        list = ExtensionLoader.getExtensionLoader(ActivateExt1.class)
                .getActivateExtension(url, "ext", "default_group");
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.get(0).getClass() == ActivateExt1Impl1.class);
        Assert.assertTrue(list.get(1).getClass() == OrderActivateExtImpl1.class);
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