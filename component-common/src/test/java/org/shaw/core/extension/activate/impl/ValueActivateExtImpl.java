package org.shaw.core.extension.activate.impl;

import org.shaw.core.extension.Activate;
import org.shaw.core.extension.activate.ActivateExt1;

@Activate(value = {"value"}, group = {"value"})
public class ValueActivateExtImpl implements ActivateExt1 {

    public String echo(String msg) {
        return msg;
    }
}
