package org.shaw.core.extension.ext1;

import org.shaw.core.extension.entity.URL;

public interface SimpleExt {

    String echo(URL url, String s);

    String yell(URL url, String s);

    // no @Adaptive
    String bang(URL url, int i);
}
