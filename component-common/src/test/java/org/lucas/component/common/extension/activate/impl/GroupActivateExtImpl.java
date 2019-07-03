package org.lucas.component.common.extension.activate.impl;

import org.lucas.component.common.extension.Activate;
import org.lucas.component.common.extension.activate.ActivateExt1;

@Activate(group = {"group1", "group2"})
public class GroupActivateExtImpl implements ActivateExt1 {

    public String echo(String msg) {
        return msg;
    }
}
