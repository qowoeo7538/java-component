package org.lucas.component.common.core.idcenter.support.buffer;

@FunctionalInterface
public interface RejectedTakeBufferHandler {

    /**
     * Reject take buffer request
     *
     * @param ringBuffer
     */
    void rejectTakeBuffer(RingBuffer ringBuffer);

}
