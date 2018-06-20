package org.shaw.core.extension.ext6_wrap.impl;

import org.shaw.core.extension.ExtURL;
import org.shaw.core.extension.ext6_wrap.WrappedExt;

public class Ext5Impl2 implements WrappedExt {
    public String echo(ExtURL url, String s) {
        return "Ext5Impl2-echo";
    }
}