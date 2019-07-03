package org.lucas.component.common.extension.ext8_add.impl;

import org.lucas.component.common.extension.ext8_add.AddExt1;

public class AddExt1_ManualAdd2 implements AddExt1 {
    public String echo(ExtURL url, String s) {
        return this.getClass().getSimpleName();
    }
}