package org.lucas.extension.ext8_add.impl;

import org.lucas.extension.ExtURL;
import org.lucas.extension.ext8_add.AddExt2;

public class AddExt2Impl1 implements AddExt2 {
    public String echo(ExtURL url, String s) {
        return this.getClass().getSimpleName();
    }
}