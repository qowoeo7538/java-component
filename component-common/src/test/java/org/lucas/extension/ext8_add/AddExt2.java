package org.lucas.extension.ext8_add;

import org.lucas.extension.Adaptive;
import org.lucas.extension.ExtURL;
import org.lucas.extension.SPI;

/**
 * show add extension pragmatically. use for test addAdaptive successful
 */
@SPI("impl1")
public interface AddExt2 {
    @Adaptive
    String echo(ExtURL url, String s);
}