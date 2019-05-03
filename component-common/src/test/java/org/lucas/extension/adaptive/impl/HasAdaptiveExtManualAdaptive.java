package org.lucas.extension.adaptive.impl;

import org.lucas.extension.Adaptive;
import org.lucas.extension.ExtURL;
import org.lucas.extension.ExtensionLoader;
import org.lucas.extension.adaptive.HasAdaptiveExt;

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
