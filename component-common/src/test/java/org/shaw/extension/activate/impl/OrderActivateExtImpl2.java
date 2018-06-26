package org.shaw.extension.activate.impl;

import org.shaw.extension.Activate;
import org.shaw.extension.activate.ActivateExt1;

@Activate(order = 2, group = {"order"})
public class OrderActivateExtImpl2 implements ActivateExt1 {

    public String echo(String msg) {
        return msg;
    }
}
