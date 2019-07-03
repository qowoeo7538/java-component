package org.lucas.component.common.extension.ext3;

import org.lucas.extension.Adaptive;
import org.lucas.extension.ExtURL;
import org.lucas.extension.SPI;

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
