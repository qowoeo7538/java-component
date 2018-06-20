package org.shaw.core.extension.ext8_add.impl;

import org.shaw.core.extension.Adaptive;
import org.shaw.core.extension.ExtURL;
import org.shaw.core.extension.ExtensionLoader;
import org.shaw.core.extension.ext8_add.AddExt4;

@Adaptive
public class AddExt4_ManualAdaptive implements AddExt4 {
    public String echo(ExtURL url, String s) {
        AddExt4 addExt1 = ExtensionLoader.getExtensionLoader(AddExt4.class).getExtension(url.getParameter("add.ext4"));
        return addExt1.echo(url, s);
    }
}