package org.lucas.extension.ext2;

import org.lucas.extension.Adaptive;
import org.lucas.extension.ExtURL;
import org.lucas.extension.SPI;

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