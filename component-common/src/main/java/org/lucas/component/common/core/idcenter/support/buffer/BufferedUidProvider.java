package org.lucas.component.common.core.idcenter.support.buffer;

import java.util.List;

@FunctionalInterface
public interface BufferedUidProvider {

    /**
     * Provides UID in one second
     *
     * @param momentInSecond
     * @return
     */
    List<Long> provide(long momentInSecond);

}
