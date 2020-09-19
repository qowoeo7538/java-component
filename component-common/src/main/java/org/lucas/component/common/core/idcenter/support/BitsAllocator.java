package org.lucas.component.common.core.idcenter.support;

import org.springframework.util.Assert;

public enum BitsAllocator {

    UID(28, 22, 13);

    /**
     * Total 64 bits
     * Bits for [sign-> second-> workId-> sequence]
     * [标示位-> 时间戳-> workId-> 自增序列]
     */
    private static final int TOTAL_BITS = 1 << 6;
    private int signBits = 1;
    private final int timestampBits;
    private final int workerIdBits;
    private final int sequenceBits;

    /**
     * 最大时间
     */
    private final long maxDeltaTimestamp;

    /**
     * 最大workId
     */
    private final long maxWorkerId;

    /**
     * 最大自增序列
     */
    private final long maxSequence;


    /**
     * 时间戳偏移位
     */
    private final int timestampShift;

    /**
     * workId 偏移位
     */
    private final int workerIdShift;

    BitsAllocator(int timestampBits, int workerIdBits, int sequenceBits) {
        // make sure allocated 64 bits
        int allocateTotalBits = signBits + timestampBits + workerIdBits + sequenceBits;
        Assert.isTrue(allocateTotalBits == TOTAL_BITS, "allocate not enough 64 bits");


        // initialize bits
        this.timestampBits = timestampBits;
        this.workerIdBits = workerIdBits;
        this.sequenceBits = sequenceBits;

        // initialize max value
        this.maxDeltaTimestamp = ~(-1L << timestampBits);
        this.maxWorkerId = ~(-1L << workerIdBits);
        this.maxSequence = ~(-1L << sequenceBits);

        // initialize shift
        this.timestampShift = workerIdBits + sequenceBits;
        this.workerIdShift = sequenceBits;
    }

    /**
     * 根据时间戳和workerId和自增序列为UID分配位
     *
     * @param deltaSeconds 时间戳
     * @param workerId     workerId
     * @param sequence     自增序列
     * @return UID
     */
    public long allocate(long deltaSeconds, long workerId, long sequence) {
        return (deltaSeconds << timestampShift) | (workerId << workerIdShift) | sequence;
    }

    /**
     * Getters
     */
    public int getSignBits() {
        return signBits;
    }

    public int getTimestampBits() {
        return timestampBits;
    }

    public int getWorkerIdBits() {
        return workerIdBits;
    }

    public int getSequenceBits() {
        return sequenceBits;
    }

    public long getMaxDeltaTimestamp() {
        return maxDeltaTimestamp;
    }

    public long getMaxWorkerId() {
        return maxWorkerId;
    }

    public long getMaxSequence() {
        return maxSequence;
    }

    public int getTimestampShift() {
        return timestampShift;
    }

    public int getWorkerIdShift() {
        return workerIdShift;
    }

}
