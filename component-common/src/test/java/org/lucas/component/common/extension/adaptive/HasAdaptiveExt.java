package org.lucas.component.common.extension.adaptive;

import org.lucas.extension.Adaptive;
import org.lucas.extension.ExtURL;
import org.lucas.extension.SPI;

@SPI
public interface HasAdaptiveExt {

    @Adaptive
    String echo(ExtURL url, String s);

}
