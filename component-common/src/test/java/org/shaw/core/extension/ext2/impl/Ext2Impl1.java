package org.shaw.core.extension.ext2.impl;

import org.shaw.core.extension.ExtURL;
import org.shaw.core.extension.ext2.Ext2;
import org.shaw.core.extension.ext2.UrlHolder;

public class Ext2Impl1 implements Ext2 {
    public String echo(UrlHolder holder, String s) {
        return "Ext2Impl1-echo";
    }

    public String bang(ExtURL url, int i) {
        return "bang1";
    }
}