package org.shaw.extension.activate;

import org.shaw.extension.SPI;

@SPI("impl1")
public interface ActivateExt1 {
    String echo(String msg);
}
