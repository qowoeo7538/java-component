package org.shaw.extension.ext6_inject.impl;

import org.junit.Assert;
import org.shaw.extension.ExtURL;
import org.shaw.extension.ext1.SimpleExt;
import org.shaw.extension.ext6_inject.Dao;
import org.shaw.extension.ext6_inject.Ext6;

public class Ext6Impl1 implements Ext6 {

    public Dao obj;

    SimpleExt ext1;

    public void setDao(Dao obj) {
        Assert.assertNotNull("inject extension instance can not be null", obj);
        Assert.fail();
    }

    public void setExt1(SimpleExt ext1) {
        this.ext1 = ext1;
    }

    public String echo(ExtURL url, String s) {
        return "Ext6Impl1-echo-" + ext1.echo(url, s);
    }


}