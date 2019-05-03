package org.lucas.extension.ext8_add;

import org.lucas.extension.Adaptive;
import org.lucas.extension.ExtURL;
import org.lucas.extension.SPI;

/**
 * show add extension pragmatically
 */
@SPI("impl1")
public interface AddExt1 {
    @Adaptive
    String echo(ExtURL url, String s);
}