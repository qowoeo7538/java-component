package org.shaw.extension.ext8_add.impl;

import org.shaw.extension.ExtURL;
import org.shaw.extension.ext8_add.AddExt1;

public class AddExt1Impl1 implements AddExt1 {
    public String echo(ExtURL url, String s) {
        return this.getClass().getSimpleName();
    }
}