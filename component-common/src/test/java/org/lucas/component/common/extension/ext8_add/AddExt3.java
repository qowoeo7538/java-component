package org.lucas.component.common.extension.ext8_add;

import org.lucas.component.common.extension.Adaptive;
import org.lucas.component.common.extension.SPI;

/**
 * show add extension pragmatically. use for test replaceAdaptive success
 */
@SPI("impl1")
public interface AddExt3 {
    @Adaptive
    String echo(ExtURL url, String s);
}