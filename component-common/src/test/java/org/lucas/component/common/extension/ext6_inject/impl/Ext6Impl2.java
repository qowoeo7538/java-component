package org.lucas.component.common.extension.ext6_inject.impl;

import org.lucas.component.common.extension.ext6_inject.Ext6;
import org.lucas.extension.ExtURL;

import java.util.List;

public class Ext6Impl2 implements Ext6 {

    List<String> list;

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public String echo(ExtURL url, String s) {
        throw new UnsupportedOperationException();
    }

}