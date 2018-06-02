package org.shaw.core.extension.adaptive;

import org.shaw.core.extension.Adaptive;
import org.shaw.core.extension.ExtURL;
import org.shaw.core.extension.SPI;

@SPI
public interface HasAdaptiveExt {

    @Adaptive
    String echo(ExtURL url, String s);

}
