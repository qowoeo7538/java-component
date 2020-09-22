package org.lucas.component.common.core.idcenter.support.time;

import org.lucas.component.common.core.idcenter.support.exception.UidGenerateException;

import java.util.concurrent.TimeUnit;

public class SecondsGenerator implements TimeGenerator {

    private long epochSeconds = TimeUnit.MILLISECONDS.toSeconds(1463673600000L);

    public SecondsGenerator() {
    }

    public SecondsGenerator(long epochSeconds) {
        this.epochSeconds = epochSeconds;
    }

    @Override
    public long genCurrentTime(long maxDeltaTime) {
        long currentSecond = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        if (currentSecond - epochSeconds > maxDeltaTime) {
            throw new UidGenerateException("Timestamp bits is exhausted. Refusing UID generate. Now: " + currentSecond);
        }
        return currentSecond;
    }

    @Override
    public long getDeltaTime(long currentTime) {
        return currentTime - epochSeconds;
    }

    @Override
    public long getThatTime(long deltaTime) {
        return epochSeconds + deltaTime;
    }
}
