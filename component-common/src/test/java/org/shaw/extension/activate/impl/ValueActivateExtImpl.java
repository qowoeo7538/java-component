package org.shaw.extension.activate.impl;

import org.shaw.extension.Activate;
import org.shaw.extension.activate.ActivateExt1;

@Activate(value = {"value"}, group = {"value"})
public class ValueActivateExtImpl implements ActivateExt1 {

    public String echo(String msg) {
        return msg;
    }
}
