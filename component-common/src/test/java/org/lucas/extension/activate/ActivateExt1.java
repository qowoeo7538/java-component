package org.lucas.extension.activate;

import org.lucas.extension.SPI;

@SPI("impl1")
public interface ActivateExt1 {
    String echo(String msg);
}