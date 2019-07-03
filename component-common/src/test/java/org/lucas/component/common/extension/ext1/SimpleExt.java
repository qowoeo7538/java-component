package org.lucas.component.common.extension.ext1;

import org.lucas.component.common.extension.Adaptive;
import org.lucas.component.common.extension.ExtURL;
import org.lucas.component.common.extension.SPI;

@SPI("impl1")
public interface SimpleExt {

    @Adaptive
    String echo(ExtURL url, String s);

    @Adaptive({"key1", "key2"})
    String yell(ExtURL url, String s);

    // no @Adaptive
    String bang(ExtURL url, int i);
}
