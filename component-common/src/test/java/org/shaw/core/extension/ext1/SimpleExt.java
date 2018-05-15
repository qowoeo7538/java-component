package org.shaw.core.extension.ext1;

import org.shaw.core.extension.Adaptive;
import org.shaw.core.extension.SPI;
import org.shaw.core.extension.entity.URL;

@SPI("impl1")
public interface SimpleExt {

    @Adaptive
    String echo(URL url, String s);

    @Adaptive({"key1", "key2"})
    String yell(URL url, String s);

    // no @Adaptive
    String bang(URL url, int i);
}
