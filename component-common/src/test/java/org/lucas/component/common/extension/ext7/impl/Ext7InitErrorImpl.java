package org.lucas.component.common.extension.ext7.impl;

import org.lucas.component.common.extension.ext7.InitErrorExt;

public class Ext7InitErrorImpl implements InitErrorExt {

    static {
        if (true) {
            throw new RuntimeException("intended!");
        }
    }

    public String echo(ExtURL url, String s) {
        return "";
    }

}