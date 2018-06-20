package org.shaw.core.extension.activate.impl;

import org.shaw.core.extension.Activate;
import org.shaw.core.extension.activate.ActivateExt1;

@Activate(group = {"default_group"})
public class ActivateExt1Impl1 implements ActivateExt1 {
    public String echo(String msg) {
        return msg;
    }
}
