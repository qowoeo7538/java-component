package org.lucas.component.common.extension.ext6_wrap.impl;


import org.lucas.component.common.extension.ExtURL;
import org.lucas.component.common.extension.ext6_wrap.WrappedExt;

import java.util.concurrent.atomic.AtomicInteger;

public class Ext5Wrapper2 implements WrappedExt {
    public static AtomicInteger echoCount = new AtomicInteger();
    WrappedExt instance;

    public Ext5Wrapper2(WrappedExt instance) {
        this.instance = instance;
    }

    public String echo(ExtURL url, String s) {
        echoCount.incrementAndGet();
        return instance.echo(url, s);
    }
}