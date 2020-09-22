package org.lucas.component.common.core.idcenter.support.time;

import cn.hutool.core.date.SystemClock;
import org.lucas.component.common.core.idcenter.support.exception.UidGenerateException;

public class MillisecondsGenerator implements TimeGenerator {

    private long epochMilliseconds = 1463673600000L;

    public MillisecondsGenerator() {
    }

    public MillisecondsGenerator(long epochMilliseconds) {
        this.epochMilliseconds = epochMilliseconds;
    }

    @Override
    public long genCurrentTime(long maxDeltaTime) {
        long currentMilliseconds = SystemClock.now();
        if (currentMilliseconds - epochMilliseconds > maxDeltaTime) {
            throw new UidGenerateException("Timestamp bits is exhausted. Refusing UID generate. Now: " + currentMilliseconds);
        }
        return currentMilliseconds;
    }

    @Override
    public long getDeltaTime(long currentTime) {
        return currentTime - epochMilliseconds;
    }

    @Override
    public long getThatTime(long deltaTime) {
        return epochMilliseconds + deltaTime;
    }
}
