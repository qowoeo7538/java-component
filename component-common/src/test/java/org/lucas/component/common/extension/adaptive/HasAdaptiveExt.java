package org.lucas.component.common.extension.adaptive;

import org.lucas.component.common.extension.Adaptive;
import org.lucas.component.common.extension.ExtURL;
import org.lucas.component.common.extension.SPI;

@SPI
public interface HasAdaptiveExt {

    @Adaptive
    String echo(ExtURL url, String s);

}
