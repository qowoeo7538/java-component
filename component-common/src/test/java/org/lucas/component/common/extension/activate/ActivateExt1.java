package org.lucas.component.common.extension.activate;

import org.lucas.component.common.extension.SPI;

@SPI("impl1")
public interface ActivateExt1 {
    String echo(String msg);
}
