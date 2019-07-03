package org.lucas.component.common.extension.adaptive.impl;

import org.lucas.component.common.extension.Adaptive;
import org.lucas.component.common.extension.ExtURL;
import org.lucas.component.common.extension.ExtensionLoader;
import org.lucas.component.common.extension.adaptive.HasAdaptiveExt;

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
