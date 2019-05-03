package org.lucas.extension.factory;

import org.lucas.extension.ExtensionLoader;
import org.lucas.extension.ExtensionFactory;
import org.lucas.extension.SPI;

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
