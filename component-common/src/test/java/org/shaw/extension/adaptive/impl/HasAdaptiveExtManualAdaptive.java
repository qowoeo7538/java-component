package org.shaw.extension.adaptive.impl;

import org.shaw.extension.Adaptive;
import org.shaw.extension.ExtURL;
import org.shaw.extension.ExtensionLoader;
import org.shaw.extension.adaptive.HasAdaptiveExt;

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
