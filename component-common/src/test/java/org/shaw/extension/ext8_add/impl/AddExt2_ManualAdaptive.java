package org.shaw.extension.ext8_add.impl;

import org.shaw.extension.Adaptive;
import org.shaw.extension.ExtURL;
import org.shaw.extension.ExtensionLoader;
import org.shaw.extension.ext8_add.AddExt2;

@Adaptive
public class AddExt2_ManualAdaptive implements AddExt2 {
    public String echo(ExtURL url, String s) {
        AddExt2 addExt1 = ExtensionLoader.getExtensionLoader(AddExt2.class).getExtension(url.getParameter("add.ext2"));
        return addExt1.echo(url, s);
    }
}