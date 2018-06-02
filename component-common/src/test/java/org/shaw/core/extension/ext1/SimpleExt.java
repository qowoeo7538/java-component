package org.shaw.core.extension.ext1;

import org.shaw.core.extension.Adaptive;
import org.shaw.core.extension.ExtURL;
import org.shaw.core.extension.SPI;

@SPI("impl1")
public interface SimpleExt {

    @Adaptive
    String echo(ExtURL url, String s);

    @Adaptive({"key1", "key2"})
    String yell(ExtURL url, String s);

    // no @Adaptive
    String bang(ExtURL url, int i);
}
