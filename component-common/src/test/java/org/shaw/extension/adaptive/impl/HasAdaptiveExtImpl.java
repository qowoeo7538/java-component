package org.shaw.extension.adaptive.impl;

import org.shaw.extension.ExtURL;
import org.shaw.extension.adaptive.HasAdaptiveExt;

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
