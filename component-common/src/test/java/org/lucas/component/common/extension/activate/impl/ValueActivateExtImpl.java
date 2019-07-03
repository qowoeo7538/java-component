package org.lucas.component.common.extension.activate.impl;

import org.lucas.extension.Activate;
import org.lucas.component.common.extension.activate.ActivateExt1;

@Activate(value = {"value"}, group = {"value"})
public class ValueActivateExtImpl implements ActivateExt1 {

    public String echo(String msg) {
        return msg;
    }
}
