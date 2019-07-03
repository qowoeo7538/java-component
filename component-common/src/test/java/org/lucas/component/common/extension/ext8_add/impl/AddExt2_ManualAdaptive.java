package org.lucas.component.common.extension.ext8_add.impl;

import org.lucas.component.common.extension.Adaptive;
import org.lucas.component.common.extension.ExtensionLoader;
import org.lucas.component.common.extension.ext8_add.AddExt2;

@Adaptive
public class AddExt2_ManualAdaptive implements AddExt2 {
    public String echo(ExtURL url, String s) {
        AddExt2 addExt1 = ExtensionLoader.getExtensionLoader(AddExt2.class).getExtension(url.getParameter("add.ext2"));
        return addExt1.echo(url, s);
    }
}