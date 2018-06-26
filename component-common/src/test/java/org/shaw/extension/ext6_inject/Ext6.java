package org.shaw.extension.ext6_inject;

import org.shaw.extension.Adaptive;
import org.shaw.extension.ExtURL;
import org.shaw.extension.SPI;

/**
 * No default
 */
@SPI
public interface Ext6 {
    @Adaptive
    String echo(ExtURL url, String s);
}