package org.shaw.core.extension.adaptive.impl;

import org.shaw.core.extension.adaptive.HasAdaptiveExt;
import org.shaw.core.extension.entity.URL;

/**
 * @create: 2018-03-06
 * @description:
 */
public class HasAdaptiveExtImpl implements HasAdaptiveExt {
    @Override
    public String echo(URL url, String s) {
        return this.getClass().getSimpleName();
    }
}
