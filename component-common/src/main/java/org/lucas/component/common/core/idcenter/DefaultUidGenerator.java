package org.lucas.component.common.core.idcenter;

import cn.hutool.core.date.DateUtil;
import org.lucas.component.common.core.idcenter.support.BitsAllocator;
import org.lucas.component.common.core.idcenter.support.exception.UidGenerateException;
import org.lucas.component.common.core.idcenter.support.time.TimeGenerator;
import org.lucas.component.common.core.idcenter.support.worker.WorkerIdAssigner;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DefaultUidGenerator {

    protected TimeGenerator timeGenerator;

    protected BitsAllocator bitsAllocator;

    protected long workerId;

    /**
     * 自增序列
     */
    protected long sequence = 0L;

    /**
     * 最后生成ID的时间
     */
    protected long lastTime = -1L;

    /**
     * @param bitsAllocator    bit 分配
     * @param workerIdAssigner worker 分配
     */
    public DefaultUidGenerator(BitsAllocator bitsAllocator, WorkerIdAssigner workerIdAssigner, TimeGenerator timeGenerator) {
        // initialize bits allocator
        if (workerId > bitsAllocator.getMaxWorkerId()) {
            throw new RuntimeException("Worker id " + workerId + " exceeds the max " + bitsAllocator.getMaxWorkerId());
        }
        this.bitsAllocator = bitsAllocator;
        workerId = workerIdAssigner.assignWorkerId();
        this.timeGenerator = timeGenerator;
    }

    public long getUID() throws UidGenerateException {
        try {
            return nextId();
        } catch (Exception e) {
            throw new UidGenerateException(e);
        }
    }

    public String parseUID(long uid) {
        long totalBits = BitsAllocator.TOTAL_BITS;
        long signBits = bitsAllocator.getSignBits();
        long timestampBits = bitsAllocator.getTimestampBits();
        long workerIdBits = bitsAllocator.getWorkerIdBits();
        long sequenceBits = bitsAllocator.getSequenceBits();

        // parse UID
        long sequence = (uid << (totalBits - sequenceBits)) >>> (totalBits - sequenceBits);
        long workerId = (uid << (timestampBits + signBits)) >>> (totalBits - workerIdBits);
        long deltaSeconds = uid >>> (workerIdBits + sequenceBits);

        Date thatTime = new Date(TimeUnit.SECONDS.toMillis(timeGenerator.getEpochTime() + deltaSeconds));
        String thatTimeStr = DateUtil.formatDateTime(thatTime);

        // format as string
        return String.format("{\"UID\":\"%d\",\"timestamp\":\"%s\",\"workerId\":\"%d\",\"sequence\":\"%d\"}",
                uid, thatTimeStr, workerId, sequence);
    }

    protected synchronized long nextId() {
        long currentTime = timeGenerator.genCurrentTime(bitsAllocator.getMaxDeltaTimestamp());
        // 1 时间回拨
        if (currentTime < lastTime) {
            // 1.1 容忍2秒内的回拨，避免NTP校时造成的异常
            if (lastTime - currentTime < 2000) {
                currentTime = lastTime;
            } else {
                // 1.2 如果服务器时间有问题(时钟后退) 报错。
                long refusedSeconds = lastTime - currentTime;
                throw new UidGenerateException("Clock moved backwards. Refusing for %d seconds", refusedSeconds);
            }
        }
        // 2 如果当前时间和上一次是同一秒时间
        if (currentTime == lastTime) {
            // 2.1 sequence自增。
            sequence = (sequence + 1) & bitsAllocator.getMaxSequence();
            // 2.2 如果 sequence 溢出则等待下一个时间。
            if (sequence == 0) {
                currentTime = getNextSecond(lastTime);
            }
        } else {
            // 3 如果是新的一秒，那么sequence重新从0开始
            sequence = 0L;
        }
        lastTime = currentTime;

        // 4 Allocate bits for UID
        return bitsAllocator.allocate(currentTime - timeGenerator.getEpochTime(), workerId, sequence);
    }

    /**
     * 等待下一秒时间
     *
     * @param lastTimestamp 最后生产ID的时间
     */
    private long getNextSecond(long lastTimestamp) {
        long timestamp = timeGenerator.genCurrentTime(bitsAllocator.getMaxDeltaTimestamp());
        while (timestamp <= lastTimestamp) {
            timestamp = timeGenerator.genCurrentTime(bitsAllocator.getMaxDeltaTimestamp());
        }
        return timestamp;
    }

}
