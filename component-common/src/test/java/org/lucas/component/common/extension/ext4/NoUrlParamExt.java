package org.lucas.component.common.extension.ext4;

import org.lucas.component.common.extension.Adaptive;
import org.lucas.component.common.extension.SPI;

import java.util.List;

@SPI("impl1")
public interface NoUrlParamExt {
    // method has no URL parameter
    @Adaptive
    String bark(String name, List<Object> list);
}