package org.shaw.extension.ext6_wrap;

import org.shaw.extension.ExtURL;
import org.shaw.extension.SPI;

/**
 * No Adaptive Method!!
 */
@SPI("impl1")
public interface WrappedExt {

    String echo(ExtURL url, String s);
}