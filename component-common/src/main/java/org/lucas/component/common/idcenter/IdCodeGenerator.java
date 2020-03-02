package org.lucas.component.common.idcenter;

import org.lucas.component.common.util.DateUtils;
import org.lucas.component.common.util.NetUtils;
import org.springframework.util.StringUtils;

import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by IntelliJ IDEA.
 * User: qiujingwang
 * Date: 2016/11/9
 * Description:与snowflake算法区别,返回字符串id,占用更多字节,但直观从id中看出生成时间(主要用于订单号、流水号等生成)
 * 注：TODO 如果有多个IP的后三位都一样，有可能会产生一样记录
 * <p>
 * 用法：建议只生成一次当前实例
 * IdCodeGenerator idCodeGenerator = new IdCodeGenerator(255, "1");
 * idCodeGenerator.nextId();
 * idCodeGenerator.nextId();
 */
public class IdCodeGenerator {

    /**
     * ip（三位IP）
     */
    private long workerId;
    /**
     * 业务编码号
     */
    private String businessCode;

    private volatile long sequence = 0L;

    /**
     * 最后三位ip（最大为255）
     */
    private long workerIdBits = 8L;
    private long maxWorkerId = -1L ^ (-1L << workerIdBits);
    private long sequenceBits = 11L;

    private long workerIdShift = sequenceBits;
    private long sequenceMask = -1L ^ (-1L << sequenceBits);

    private volatile long lastTimestamp = timeGen();

    private static int ip = 0;

    static {
        try {
            String ipStr = NetUtils.getLocalIp().substring(NetUtils.getLocalIp().length() - 3, NetUtils.getLocalIp().length());
            ip = Integer.parseInt(ipStr);
        } catch (final SocketException | UnknownHostException e) {
            throw new IllegalCallerException("");
        }
    }

    /**
     * @param businessCode 业务编码号 如：0、1、2
     */
    public IdCodeGenerator(String businessCode) {
        int workerId = ip;
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (StringUtils.isEmpty(businessCode)) {
            throw new IllegalArgumentException("businessCode can't be empty");
        }

        this.workerId = workerId;
        this.businessCode = businessCode;
    }

    /**
     * @param businessCode 业务编码号 如：0、1、2
     */
    public IdCodeGenerator(long workerId, String businessCode) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (StringUtils.isEmpty(businessCode)) {
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
        String datePrefix = DateUtils.formatDate(timestamp, DateUtils.YYMMDDHHMMSSSSS);
        return new StringBuilder(25).append(datePrefix.substring(0, DateUtils.YYMMDDHHMMSS.length())).append(businessCode).append(suffix).append(datePrefix.substring(DateUtils.YYMMDDHHMMSS.length())).toString();
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
    }

}