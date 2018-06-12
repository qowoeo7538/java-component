package org.shaw.core.extension.ext3;

import org.shaw.core.extension.Adaptive;
import org.shaw.core.extension.ExtURL;
import org.shaw.core.extension.SPI;

/**
 * @create: 2018-06-08
 * @description:
 */
@SPI("impl1")
public interface UseProtocolKeyExt {

    // protocol key is the second
    @Adaptive({"key1", "protocol"})
    String echo(ExtURL url, String s);

    // protocol key is the first
    @Adaptive({"protocol", "key2"})
    String yell(ExtURL url, String s);

}
