package org.shaw.core.extension;

@SPI
public interface ExtensionFactory {

    <T> T getExtension(Class<T> type, String name);

}
