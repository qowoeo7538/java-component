package org.lucas.extension.activate.impl;

import org.lucas.extension.Activate;
import org.lucas.extension.activate.ActivateExt1;

@Activate(group = {"default_group"})
public class ActivateExt1Impl1 implements ActivateExt1 {
    public String echo(String msg) {
        return msg;
    }
}