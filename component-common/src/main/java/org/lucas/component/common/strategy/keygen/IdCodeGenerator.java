package org.lucas.component.common.strategy.keygen;

public class IdCodeGenerator {

    /**
     * ip（三位IP）
     *//*
    private long workerId;

    *//**
     * 业务编码号
     *//*
    private String businessCode;

    private volatile long sequence = 0L;

    *//**
     * 最后三位ip（最大为255）
     *//*
    private long workerIdBits = 8L;
    private long maxWorkerId = -1L ^ (-1L << workerIdBits);
    private long sequenceBits = 11L;

    private long workerIdShift = sequenceBits;
    private long sequenceMask = -1L ^ (-1L << sequenceBits);

    private volatile long lastTimestamp = timeGen();

    private static int ip = 0;

    static {
        String ipStr = LocalHostUtil.getIpCode().substring(LocalHostUtil.getIpCode().length() - 3, LocalHostUtil.getIpCode().length());
        ip = Integer.parseInt(ipStr);
    }

    *//**
     * @param businessCode 业务编码号 如：0、1、2
     *//*
    public IdCodeGenerator(String businessCode) {

        int workerId = ip;//Integer.parseInt(ip);

        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (StringUtil.isBlank(businessCode)) {
            throw new IllegalArgumentException("businessCode can't be empty");
        }

        this.workerId = workerId;
        this.businessCode = businessCode;
        log.info(String.format("IdCodeGenerator starting. timestamp is %s, businessCode is %s, workerId is %d, worker id bits %d, sequence bits %d", DateUtil.YYMMDDHHMMSSSSS, businessCode, workerId, workerIdBits, sequenceBits));
    }

    *//**
     * @param businessCode 业务编码号 如：0、1、2
     *//*
    public IdCodeGenerator(long workerId, String businessCode) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (StringUtil.isBlank(businessCode)) {
            throw new IllegalArgumentException("businessCode can't be empty");
        }
        this.workerId = workerId;
        this.businessCode = businessCode;
    }

    public String nextId() {
        long timestamp;
        long seqNum = 0;
        synchronized (this) {
            timestamp = timeGen();
            if (timestamp < lastTimestamp) {

                throw new RuntimeException
                        (String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
            }
            if (lastTimestamp == timestamp) {
                sequence = (sequence + 1) & sequenceMask;
                if (sequence == 0) {
                    timestamp = tilNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0L;
            }

            seqNum = sequence;
            lastTimestamp = timestamp;
        }

        long suffix = (workerId << workerIdShift) | seqNum;
        String datePrefix = DateUtil.formatDate(timestamp, DateUtil.YYMMDDHHMMSSSSS);
        return new StringBuilder(25).append(datePrefix.substring(0, DateUtil.YYMMDDHHMMSS.length())).append(businessCode).append(suffix).append(datePrefix.substring(DateUtil.YYMMDDHHMMSS.length())).toString();
    }

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    protected long timeGen() {
        return System.currentTimeMillis();
    }*/


}
