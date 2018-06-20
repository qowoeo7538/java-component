package org.shaw.core.extension.activate;

import org.shaw.core.extension.SPI;

@SPI("impl1")
public interface ActivateExt1 {
    String echo(String msg);
}
