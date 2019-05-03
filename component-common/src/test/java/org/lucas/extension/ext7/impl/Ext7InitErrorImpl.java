package org.lucas.extension.ext7.impl;

import org.lucas.extension.ExtURL;
import org.lucas.extension.ext7.InitErrorExt;

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