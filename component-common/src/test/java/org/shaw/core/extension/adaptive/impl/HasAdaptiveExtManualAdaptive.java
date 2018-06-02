package org.shaw.core.extension.adaptive.impl;

import org.shaw.core.extension.Adaptive;
import org.shaw.core.extension.ExtURL;
import org.shaw.core.extension.ExtensionLoader;
import org.shaw.core.extension.adaptive.HasAdaptiveExt;

/**
 * @create: 2018-03-01
 * @description:
 */
@Adaptive
public class HasAdaptiveExtManualAdaptive implements HasAdaptiveExt {

    @Override
    public String echo(ExtURL url, String s) {
        HasAdaptiveExt addExt1 = ExtensionLoader.getExtensionLoader(HasAdaptiveExt.class)
                .getExtension(url.getParameter("key"));
        return addExt1.echo(url, s);
    }
}
