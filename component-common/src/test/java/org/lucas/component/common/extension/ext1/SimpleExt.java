package org.lucas.component.common.extension.ext1;

import org.lucas.extension.Adaptive;
import org.lucas.extension.ExtURL;
import org.lucas.extension.SPI;

@SPI("impl1")
public interface SimpleExt {

    @Adaptive
    String echo(ExtURL url, String s);

    @Adaptive({"key1", "key2"})
    String yell(ExtURL url, String s);

    // no @Adaptive
    String bang(ExtURL url, int i);
}
