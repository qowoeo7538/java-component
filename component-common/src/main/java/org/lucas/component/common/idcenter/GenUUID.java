package org.lucas.component.common.idcenter;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class GenUUID {
    static String DASH = "-";

    public static String get32UUID() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return new UUID(random.nextLong(), random.nextLong()).toString().replace(DASH, "");
    }
}
