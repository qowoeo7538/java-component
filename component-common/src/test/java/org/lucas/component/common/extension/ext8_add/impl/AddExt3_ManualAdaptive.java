package org.lucas.component.common.extension.ext8_add.impl;

import org.lucas.component.common.extension.ext8_add.AddExt3;
import org.lucas.component.common.extension.Adaptive;
import org.lucas.component.common.extension.ExtensionLoader;

@Adaptive
public class AddExt3_ManualAdaptive implements AddExt3 {
    public String echo(ExtURL url, String s) {
        AddExt3 addExt1 = ExtensionLoader.getExtensionLoader(AddExt3.class).getExtension(url.getParameter("add.ext3"));
        return addExt1.echo(url, s);
    }
}