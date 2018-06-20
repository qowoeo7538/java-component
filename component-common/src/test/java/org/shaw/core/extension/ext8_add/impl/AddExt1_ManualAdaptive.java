package org.shaw.core.extension.ext8_add.impl;


import org.shaw.core.extension.Adaptive;
import org.shaw.core.extension.ExtURL;
import org.shaw.core.extension.ExtensionLoader;
import org.shaw.core.extension.ext8_add.AddExt1;

@Adaptive
public class AddExt1_ManualAdaptive implements AddExt1 {
    public String echo(ExtURL url, String s) {
        AddExt1 addExt1 = ExtensionLoader.getExtensionLoader(AddExt1.class).getExtension(url.getParameter("add.ext1"));
        return addExt1.echo(url, s);
    }
}