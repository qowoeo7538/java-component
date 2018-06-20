package org.shaw.core.extension.activate.impl;

import org.shaw.core.extension.Activate;
import org.shaw.core.extension.activate.ActivateExt1;

@Activate(order = 1, group = {"order"})
public class OrderActivateExtImpl1 implements ActivateExt1 {

    public String echo(String msg) {
        return msg;
    }
}
