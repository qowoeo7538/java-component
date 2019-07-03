package org.lucas.component.common.extension.ext10_abstract;

import org.lucas.extension.SPI;

/**
 * @create: 2018-06-22
 * @description:
 */
@SPI("impl1")
public abstract class SimpleExt10 {

    public String print() {
        StringBuilder message = new StringBuilder("abstract-print\n");
        message.append(call());
        return message.toString();
    }

    public abstract String call();
}
