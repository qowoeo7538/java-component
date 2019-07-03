package org.lucas.component.common.extension.ext6_wrap.impl;

import org.lucas.component.common.extension.ExtURL;
import org.lucas.component.common.extension.ext6_wrap.WrappedExt;

public class Ext5Impl1 implements WrappedExt {
    public String echo(ExtURL url, String s) {
        return "Ext5Impl1-echo";
    }
}