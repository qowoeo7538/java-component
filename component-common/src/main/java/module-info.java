module component.common {

    // spring
    requires transitive spring.core;

    // collections
    requires transitive org.eclipse.collections.api;
    requires transitive org.eclipse.collections.impl;

    // java compiler
    requires java.compiler;
    requires javassist;

    // log
    requires java.logging;

    // java management
    requires java.management;

    // ================================================

    exports org.lucas.component.common.core;
    exports org.lucas.component.common.compiler;
    exports org.lucas.component.common.extension to ExtensionLoader;
    exports org.lucas.component.common.io;
    exports org.lucas.component.common.util;
}