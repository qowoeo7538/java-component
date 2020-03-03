package org.lucas.component.common.idcenter;

import java.net.NetworkInterface;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Enumeration;

public class SeqWorker {

    private final static long twepoch = 1288834974657L;
    private long sequence = 0L;
    private final static long sequenceBits = 10L;

    private final static long timestampLeftShift = sequenceBits;
    public final static long sequenceMask = -1L ^ -1L << sequenceBits;

    private long lastTimestamp = -1L;


    private static final int LOW_ORDER_THREE_BYTES = 0x00ffffff;

    private static final int MACHINE_IDENTIFIER;
    private static final short PROCESS_IDENTIFIER;
    private static final long processBits = 16L;

    private final String workerId;

    /**
     * @param businessCode 业务编码号 如：0、1、2
     */
    public SeqWorker(int businessCode) {
        long counter = MACHINE_IDENTIFIER;
        if (businessCode < 0) {
            throw new IllegalArgumentException("businessCode can't be less than 0");
        }
        workerId = ((counter << processBits) | PROCESS_IDENTIFIER) + businessCode + "";
    }

    public String nextId() {
        long seqNum;
        long timestamp;
        synchronized (this) {
            timestamp = this.timeGen();

            if (this.lastTimestamp == timestamp) {
                this.sequence = (this.sequence + 1) & SeqWorker.sequenceMask;
                if (this.sequence == 0) {
//                System.out.println("###########" + sequenceMask);
                    timestamp = this.tilNextMillis(this.lastTimestamp);
                }
            } else {
                this.sequence = 0;
            }
            if (timestamp < this.lastTimestamp) {
                throw new RuntimeException(
                        String.format(
                                "Clock moved backwards.  Refusing to generate id for %d milliseconds",
                                this.lastTimestamp - timestamp));
            }

            this.lastTimestamp = timestamp;
            seqNum = this.sequence;
        }

        long nextId = (((timestamp - twepoch) << timestampLeftShift)) | (seqNum);
        return new StringBuilder(32).append(workerId).append(nextId).toString();
    }

    private long tilNextMillis(final long lastTimestamp) {
        long timestamp = this.timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = this.timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }

    static {
        try {
            MACHINE_IDENTIFIER = createMachineIdentifier();
            PROCESS_IDENTIFIER = createProcessIdentifier();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int createMachineIdentifier() {
        // build a 2-byte machine piece based on NICs info
        int machinePiece;
        try {
            StringBuilder sb = new StringBuilder();
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface ni = e.nextElement();
                sb.append(ni.toString());
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    ByteBuffer bb = ByteBuffer.wrap(mac);
                    try {
                        sb.append(bb.getChar());
                        sb.append(bb.getChar());
                        sb.append(bb.getChar());
                    } catch (BufferUnderflowException shortHardwareAddressException) { //NOPMD
                        // mac with less than 6 bytes. continue
                    }
                }
            }
            machinePiece = sb.toString().hashCode();
        } catch (Throwable t) {
            // exception sometimes happens with IBM JVM, use random
            machinePiece = (new SecureRandom().nextInt());
        }
        machinePiece = machinePiece & LOW_ORDER_THREE_BYTES;
        return machinePiece;
    }

    // Creates the process identifier.  This does not have to be unique per class loader because
    // NEXT_COUNTER will provide the uniqueness.
    private static short createProcessIdentifier() {
        short processId;
        try {
            String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
            if (processName.contains("@")) {
                processId = (short) Integer.parseInt(processName.substring(0, processName.indexOf('@')));
            } else {
                processId = (short) java.lang.management.ManagementFactory.getRuntimeMXBean().getName().hashCode();
            }

        } catch (Throwable t) {
            processId = (short) new SecureRandom().nextInt();
        }

        return processId;
    }

}
