package org.shaw.core.extension.ext6_inject;

import org.shaw.core.extension.Adaptive;
import org.shaw.core.extension.ExtURL;
import org.shaw.core.extension.SPI;

/**
 * No default
 */
@SPI
public interface Ext6 {
    @Adaptive
    String echo(ExtURL url, String s);
}