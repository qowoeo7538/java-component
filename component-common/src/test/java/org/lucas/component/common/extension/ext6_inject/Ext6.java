package org.lucas.component.common.extension.ext6_inject;

import org.lucas.component.common.extension.Adaptive;
import org.lucas.component.common.extension.SPI;

/**
 * No default
 */
@SPI
public interface Ext6 {
    @Adaptive
    String echo(ExtURL url, String s);
}