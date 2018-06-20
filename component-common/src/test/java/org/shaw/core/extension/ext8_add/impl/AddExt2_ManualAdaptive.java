package org.shaw.core.extension.ext8_add.impl;

import org.shaw.core.extension.Adaptive;
import org.shaw.core.extension.ExtURL;
import org.shaw.core.extension.ExtensionLoader;
import org.shaw.core.extension.ext8_add.AddExt2;

@Adaptive
public class AddExt2_ManualAdaptive implements AddExt2 {
    public String echo(ExtURL url, String s) {
        AddExt2 addExt1 = ExtensionLoader.getExtensionLoader(AddExt2.class).getExtension(url.getParameter("add.ext2"));
        return addExt1.echo(url, s);
    }
}