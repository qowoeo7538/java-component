package org.lucas.component.common.extension.ext8_add;

import org.lucas.component.common.extension.Adaptive;
import org.lucas.component.common.extension.ExtURL;
import org.lucas.component.common.extension.SPI;

/**
 * show add extension pragmatically. use for test addAdaptive successful
 */
@SPI("impl1")
public interface AddExt2 {
    @Adaptive
    String echo(ExtURL url, String s);
}