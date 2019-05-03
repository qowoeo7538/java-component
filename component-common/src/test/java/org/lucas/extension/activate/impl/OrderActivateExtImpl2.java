package org.lucas.extension.activate.impl;

import org.lucas.extension.Activate;
import org.lucas.extension.activate.ActivateExt1;

@Activate(order = 2, group = {"order"})
public class OrderActivateExtImpl2 implements ActivateExt1 {

    public String echo(String msg) {
        return msg;
    }
}
