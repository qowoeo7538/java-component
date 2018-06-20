package org.shaw.core.extension.ext7;

import org.shaw.core.extension.Adaptive;
import org.shaw.core.extension.ExtURL;
import org.shaw.core.extension.SPI;

@SPI
public interface InitErrorExt {
    @Adaptive
    String echo(ExtURL url, String s);
}