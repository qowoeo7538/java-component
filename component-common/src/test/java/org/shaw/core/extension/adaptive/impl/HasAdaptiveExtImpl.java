package org.shaw.core.extension.adaptive.impl;

import org.shaw.core.extension.ExtURL;
import org.shaw.core.extension.adaptive.HasAdaptiveExt;

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
