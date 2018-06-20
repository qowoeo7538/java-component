package org.shaw.core.extension.ext8_add.impl;

import org.shaw.core.extension.ExtURL;
import org.shaw.core.extension.ext8_add.AddExt1;

public class AddExt1_ManualAdd1 implements AddExt1 {
    public String echo(ExtURL url, String s) {
        return this.getClass().getSimpleName();
    }
}