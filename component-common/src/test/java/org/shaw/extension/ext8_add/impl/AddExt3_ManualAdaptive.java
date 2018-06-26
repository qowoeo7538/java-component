package org.shaw.extension.ext8_add.impl;

import org.shaw.extension.Adaptive;
import org.shaw.extension.ExtURL;
import org.shaw.extension.ExtensionLoader;
import org.shaw.extension.ext8_add.AddExt3;

@Adaptive
public class AddExt3_ManualAdaptive implements AddExt3 {
    public String echo(ExtURL url, String s) {
        AddExt3 addExt1 = ExtensionLoader.getExtensionLoader(AddExt3.class).getExtension(url.getParameter("add.ext3"));
        return addExt1.echo(url, s);
    }
}