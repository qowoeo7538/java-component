package org.shaw.extension.ext8_add.impl;


import org.shaw.extension.Adaptive;
import org.shaw.extension.ExtURL;
import org.shaw.extension.ExtensionLoader;
import org.shaw.extension.ext8_add.AddExt1;

@Adaptive
public class AddExt1_ManualAdaptive implements AddExt1 {
    public String echo(ExtURL url, String s) {
        AddExt1 addExt1 = ExtensionLoader.getExtensionLoader(AddExt1.class).getExtension(url.getParameter("add.ext1"));
        return addExt1.echo(url, s);
    }
}