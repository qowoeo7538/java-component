package org.lucas.component.common.core.idcenter.support.time;

public interface TimeGenerator {

    /**
     * 生成当前时间
     */
    long genCurrentTime(long maxDeltaTime);

    long getEpochTime();
}
