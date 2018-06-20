package org.shaw.core.extension.ext8_add;

import org.shaw.core.extension.Adaptive;
import org.shaw.core.extension.ExtURL;
import org.shaw.core.extension.SPI;

/**
 * show add extension pragmatically
 */
@SPI("impl1")
public interface AddExt1 {
    @Adaptive
    String echo(ExtURL url, String s);
}