package org.lucas.component.common.extension.ext8_add.impl;

import org.lucas.component.common.extension.ExtURL;
import org.lucas.component.common.extension.ext8_add.AddExt2;

public class AddExt2Impl1 implements AddExt2 {
    public String echo(ExtURL url, String s) {
        return this.getClass().getSimpleName();
    }
}