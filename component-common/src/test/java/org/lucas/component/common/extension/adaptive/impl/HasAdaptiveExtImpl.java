package org.lucas.component.common.extension.adaptive.impl;

import org.lucas.extension.ExtURL;
import org.lucas.component.common.extension.adaptive.HasAdaptiveExt;

/**
 * @create: 2018-03-06
 * @description:
 */
public class HasAdaptiveExtImpl implements HasAdaptiveExt {

    @Override
    public String echo(ExtURL url, String s) {
        return this.getClass().getSimpleName();
    }
}
