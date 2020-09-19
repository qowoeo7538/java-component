package org.lucas.component.common.core.idcenter;

import java.net.NetworkInterface;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Enumeration;

/**
 * 编码构成： wokerId()-> 时间戳-> workerId(8 bit)-> 自增序列(11 bit)
 */
public class SeqGenerator {

    /**
     * 项目起始时间
     */
    private static final long TIME_EPOCH = 1463673600000L;

    /**
     * 初始化序列
     */
    private long sequence = 0L;

    /**
     * 自增 bits
     */
    private static final long sequenceBits = 10L;

    /**
     * 自增序列最大值：1023
     */
    public static final long sequenceMask = ~(-1L << sequenceBits);

    /**
     * 时间戳偏移量
     */
    private static final long timestampLeftShift = sequenceBits;

    /**
     * 初始化时间差
     */
    private long lastTimestamp = -1L;

    private static final int LOW_ORDER_THREE_BYTES = 0x00ffffff;

    private static final int MACHINE_IDENTIFIER;
    private static final short PROCESS_IDENTIFIER;
    private static final long processBits = 16L;

    private final String workerId;

    static {
        try {
            MACHINE_IDENTIFIER = createMachineIdentifier();
            PROCESS_IDENTIFIER = createProcessIdentifier();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param businessCode 业务编码号 如：0、1、2
     */
    public SeqGenerator(int businessCode) {
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
            if (timestamp < this.lastTimestamp) {
                throw new RuntimeException(
                        String.format(
                                "Clock moved backwards.  Refusing to generate id for %d milliseconds",
                                this.lastTimestamp - timestamp));
            }
            if (this.lastTimestamp == timestamp) {
                this.sequence = (this.sequence + 1) & SeqGenerator.sequenceMask;
                if (this.sequence == 0) {
                    timestamp = this.tilNextMillis(this.lastTimestamp);
                }
            } else {
                this.sequence = 0;
            }
            this.lastTimestamp = timestamp;
            seqNum = this.sequence;
        }
        long nextId = (((timestamp - TIME_EPOCH) << timestampLeftShift)) | (seqNum);
        return workerId + nextId;
    }

    private long tilNextMillis(final long lastTimestamp) {
        long timestamp = this.timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = this.timeGen();
        }
        return timestamp;
    }

    /**
     * @return 当前时间戳
     */
    private long timeGen() {
        return System.currentTimeMillis();
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
