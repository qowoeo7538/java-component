package org.lucas.component.common.extension.ext8_add;

import org.lucas.component.common.extension.Adaptive;
import org.lucas.component.common.extension.SPI;

/**
 * show add extension pragmatically. use for test replaceAdaptive fail
 */
@SPI("impl1")
public interface AddExt4 {
    @Adaptive
    String echo(ExtURL url, String s);
}