package org.shaw.core.extension.ext6_wrap;

import org.shaw.core.extension.ExtURL;
import org.shaw.core.extension.SPI;

/**
 * No Adaptive Method!!
 */
@SPI("impl1")
public interface WrappedExt {

    String echo(ExtURL url, String s);
}