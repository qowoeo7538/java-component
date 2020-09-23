package org.lucas.component.common.core.idcenter.support.buffer;

@FunctionalInterface
public interface RejectedPutBufferHandler {

    /**
     * Reject put buffer request
     *
     * @param ringBuffer
     * @param uid
     */
    void rejectPutBuffer(RingBuffer ringBuffer, long uid);

}
