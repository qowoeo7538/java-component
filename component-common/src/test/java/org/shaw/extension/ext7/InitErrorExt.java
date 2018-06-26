package org.shaw.extension.ext7;

import org.shaw.extension.Adaptive;
import org.shaw.extension.ExtURL;
import org.shaw.extension.SPI;

@SPI
public interface InitErrorExt {
    @Adaptive
    String echo(ExtURL url, String s);
}