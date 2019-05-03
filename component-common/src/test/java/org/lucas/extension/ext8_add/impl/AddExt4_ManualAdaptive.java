package org.lucas.extension.ext8_add.impl;

import org.lucas.extension.Adaptive;
import org.lucas.extension.ExtURL;
import org.lucas.extension.ExtensionLoader;
import org.lucas.extension.ext8_add.AddExt4;

@Adaptive
public class AddExt4_ManualAdaptive implements AddExt4 {
    public String echo(ExtURL url, String s) {
        AddExt4 addExt1 = ExtensionLoader.getExtensionLoader(AddExt4.class).getExtension(url.getParameter("add.ext4"));
        return addExt1.echo(url, s);
    }
}