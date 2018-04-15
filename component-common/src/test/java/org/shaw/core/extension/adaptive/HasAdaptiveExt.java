package org.shaw.core.extension.adaptive;

import org.shaw.core.extension.Adaptive;
import org.shaw.core.extension.SPI;
import org.shaw.core.extension.entity.URL;

@SPI
public interface HasAdaptiveExt {

    @Adaptive
    String echo(URL url, String s);

}
