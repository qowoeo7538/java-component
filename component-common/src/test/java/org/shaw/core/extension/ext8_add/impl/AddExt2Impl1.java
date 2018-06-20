package org.shaw.core.extension.ext8_add.impl;

import org.shaw.core.extension.ExtURL;
import org.shaw.core.extension.ext8_add.AddExt2;

public class AddExt2Impl1 implements AddExt2 {
    public String echo(ExtURL url, String s) {
        return this.getClass().getSimpleName();
    }
}