package org.shaw.extension.ext8_add;

import org.shaw.extension.Adaptive;
import org.shaw.extension.ExtURL;
import org.shaw.extension.SPI;

/**
 * show add extension pragmatically. use for test addAdaptive successful
 */
@SPI("impl1")
public interface AddExt2 {
    @Adaptive
    String echo(ExtURL url, String s);
}