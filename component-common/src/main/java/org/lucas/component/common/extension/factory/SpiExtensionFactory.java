package org.lucas.component.common.extension.factory;

import org.lucas.component.common.extension.ExtensionLoader;
import org.lucas.component.common.extension.ExtensionFactory;
import org.lucas.component.common.extension.SPI;

/**
 * @create: 2018-03-06
 * @description:
 */
public class SpiExtensionFactory implements ExtensionFactory {
    @Override
    public <T> T getExtension(Class<T> type, String name) {
        if (type.isInterface() && type.isAnnotationPresent(SPI.class)) {
            ExtensionLoader<T> loader = ExtensionLoader.getExtensionLoader(type);
            if (!loader.getSupportedExtensions().isEmpty()) {
                return loader.getAdaptiveExtension();
            }
        }
        return null;
    }
}
