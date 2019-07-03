package org.lucas.component.common.extension.ext8_add.impl;


import org.lucas.component.common.extension.ext8_add.AddExt1;
import org.lucas.component.common.extension.Adaptive;
import org.lucas.component.common.extension.ExtensionLoader;

@Adaptive
public class AddExt1_ManualAdaptive implements AddExt1 {
    public String echo(ExtURL url, String s) {
        AddExt1 addExt1 = ExtensionLoader.getExtensionLoader(AddExt1.class).getExtension(url.getParameter("add.ext1"));
        return addExt1.echo(url, s);
    }
}