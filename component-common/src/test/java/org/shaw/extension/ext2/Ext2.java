package org.shaw.extension.ext2;

import org.shaw.extension.Adaptive;
import org.shaw.extension.ExtURL;
import org.shaw.extension.SPI;

/**
 * Has no default
 */
@SPI
public interface Ext2 {
    // one of the properties of an argument is an instance of URL.
    @Adaptive
    String echo(UrlHolder holder, String s);

    String bang(ExtURL url, int i);
}