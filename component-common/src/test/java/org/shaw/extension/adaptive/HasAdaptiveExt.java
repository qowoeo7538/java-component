package org.shaw.extension.adaptive;

import org.shaw.extension.Adaptive;
import org.shaw.extension.ExtURL;
import org.shaw.extension.SPI;

@SPI
public interface HasAdaptiveExt {

    @Adaptive
    String echo(ExtURL url, String s);

}
