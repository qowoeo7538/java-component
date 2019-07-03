package org.lucas.component.common.extension.ext2.impl;

import org.lucas.component.common.extension.ext2.Ext2;
import org.lucas.component.common.extension.ext2.UrlHolder;

public class Ext2Impl2 implements Ext2 {
    public String echo(UrlHolder holder, String s) {
        return "Ext2Impl2-echo";
    }

    public String bang(ExtURL url, int i) {
        return "bang2";
    }

}