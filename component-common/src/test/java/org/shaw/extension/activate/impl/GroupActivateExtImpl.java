package org.shaw.extension.activate.impl;

import org.shaw.extension.Activate;
import org.shaw.extension.activate.ActivateExt1;

@Activate(group = {"group1", "group2"})
public class GroupActivateExtImpl implements ActivateExt1 {

    public String echo(String msg) {
        return msg;
    }
}