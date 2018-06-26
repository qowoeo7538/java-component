package org.shaw.extension.ext7.impl;

import org.shaw.extension.ExtURL;
import org.shaw.extension.ext7.InitErrorExt;

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