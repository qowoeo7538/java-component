package org.lucas.component.common.extension.ext7;

import org.lucas.component.common.extension.Adaptive;
import org.lucas.component.common.extension.ExtURL;
import org.lucas.component.common.extension.SPI;

@SPI
public interface InitErrorExt {
    @Adaptive
    String echo(ExtURL url, String s);
}