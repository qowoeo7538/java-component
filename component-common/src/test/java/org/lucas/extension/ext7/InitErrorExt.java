package org.lucas.extension.ext7;

import org.lucas.extension.Adaptive;
import org.lucas.extension.ExtURL;
import org.lucas.extension.SPI;

@SPI
public interface InitErrorExt {
    @Adaptive
    String echo(ExtURL url, String s);
}