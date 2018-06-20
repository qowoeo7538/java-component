package org.shaw.core.extension.ext8_add;

import org.shaw.core.extension.Adaptive;
import org.shaw.core.extension.ExtURL;
import org.shaw.core.extension.SPI;

/**
 * show add extension pragmatically. use for test addAdaptive successful
 */
@SPI("impl1")
public interface AddExt2 {
    @Adaptive
    String echo(ExtURL url, String s);
}