package org.shaw.core.extension.activate.impl;

import org.shaw.core.extension.Activate;
import org.shaw.core.extension.activate.ActivateExt1;

@Activate(order = 2, group = {"order"})
public class OrderActivateExtImpl2 implements ActivateExt1 {

    public String echo(String msg) {
        return msg;
    }
}
