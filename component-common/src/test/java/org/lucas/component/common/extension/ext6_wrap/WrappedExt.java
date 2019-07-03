package org.lucas.component.common.extension.ext6_wrap;

import org.lucas.component.common.extension.ExtURL;
import org.lucas.component.common.extension.SPI;

/**
 * No Adaptive Method!!
 */
@SPI("impl1")
public interface WrappedExt {

    String echo(ExtURL url, String s);
}