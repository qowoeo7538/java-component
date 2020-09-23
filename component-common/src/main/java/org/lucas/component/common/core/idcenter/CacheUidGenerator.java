package org.lucas.component.common.core.idcenter;

import org.lucas.component.common.core.idcenter.support.BitsAllocator;
import org.lucas.component.common.core.idcenter.support.buffer.BufferPaddingExecutor;
import org.lucas.component.common.core.idcenter.support.buffer.RejectedPutBufferHandler;
import org.lucas.component.common.core.idcenter.support.buffer.RejectedTakeBufferHandler;
import org.lucas.component.common.core.idcenter.support.buffer.RingBuffer;
import org.lucas.component.common.core.idcenter.support.exception.UidGenerateException;
import org.lucas.component.common.core.idcenter.support.time.SecondsGenerator;
import org.lucas.component.common.core.idcenter.support.worker.WorkerIdAssigner;

import java.util.ArrayList;
import java.util.List;

public class CacheUidGenerator extends DefaultUidGenerator {

    private static final int DEFAULT_BOOST_POWER = 3;
    private int boostPower = DEFAULT_BOOST_POWER;

    private int paddingFactor = RingBuffer.DEFAULT_PADDING_PERCENT;
    private Long scheduleInterval;

    private RejectedPutBufferHandler rejectedPutBufferHandler;
    private RejectedTakeBufferHandler rejectedTakeBufferHandler;

    /**
     * RingBuffer
     */
    private RingBuffer ringBuffer;
    private BufferPaddingExecutor bufferPaddingExecutor;

    /**
     * @param workerIdAssigner worker 分配
     */
    public CacheUidGenerator(WorkerIdAssigner workerIdAssigner) {
        super(BitsAllocator.UID, workerIdAssigner, new SecondsGenerator());
        this.initRingBuffer();
    }

    @Override
    public long getUID() {
        try {
            return ringBuffer.take();
        } catch (Exception e) {
            throw new UidGenerateException(e);
        }
    }

    @Override
    public String parseUID(long uid) {
        return super.parseUID(uid);
    }

    public void destroy() throws Exception {
        bufferPaddingExecutor.shutdown();
    }

    /**
     * Get the UIDs in the same specified second under the max sequence
     *
     * @param currentSecond
     * @return UID list, size of {@link BitsAllocator#getMaxSequence()} + 1
     */
    protected List<Long> nextIdsForOneSecond(long currentSecond) {
        int listSize = (int) bitsAllocator.getMaxSequence() + 1;
        List<Long> uidList = new ArrayList<>(listSize);
        long firstSeqUid = bitsAllocator.allocate(currentSecond - timeGenerator.getEpochTime(), workerId, 0L);
        for (int offset = 0; offset < listSize; offset++) {
            uidList.add(firstSeqUid + offset);
        }
        return uidList;
    }

    private void initRingBuffer() {
        // initialize RingBuffer
        int bufferSize = ((int) bitsAllocator.getMaxSequence() + 1) << boostPower;
        this.ringBuffer = new RingBuffer(bufferSize, paddingFactor);

        // initialize RingBufferPaddingExecutor
        boolean usingSchedule = (scheduleInterval != null);
        this.bufferPaddingExecutor = new BufferPaddingExecutor(ringBuffer, this::nextIdsForOneSecond, usingSchedule);
        if (usingSchedule) {
            bufferPaddingExecutor.setScheduleInterval(scheduleInterval);
        }
        this.ringBuffer.setBufferPaddingExecutor(bufferPaddingExecutor);
        if (rejectedPutBufferHandler != null) {
            this.ringBuffer.setRejectedPutHandler(rejectedPutBufferHandler);
        }
        if (rejectedTakeBufferHandler != null) {
            this.ringBuffer.setRejectedTakeHandler(rejectedTakeBufferHandler);
        }

        // fill in all slots of the RingBuffer
        bufferPaddingExecutor.paddingBuffer();

        // start buffer padding threads
        bufferPaddingExecutor.start();
    }

}
