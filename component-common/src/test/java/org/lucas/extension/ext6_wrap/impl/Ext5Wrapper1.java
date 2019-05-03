package org.lucas.extension.ext6_wrap.impl;

import org.lucas.extension.ExtURL;
import org.lucas.extension.ext6_wrap.WrappedExt;

import java.util.concurrent.atomic.AtomicInteger;

public class Ext5Wrapper1 implements WrappedExt {

    public static AtomicInteger echoCount = new AtomicInteger();
    WrappedExt instance;

    public Ext5Wrapper1(WrappedExt instance) {
        this.instance = instance;
    }

    public String echo(ExtURL url, String s) {
        echoCount.incrementAndGet();
        return instance.echo(url, s);
    }
}