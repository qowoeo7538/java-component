package org.lucas.component.common.generator.idcenter;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.SystemClock;
import cn.hutool.core.util.StrUtil;
import org.lucas.component.common.core.date.DatePattern;
import org.lucas.component.common.core.date.DateUtils;
import org.lucas.component.common.util.NetUtils;
import org.springframework.util.StringUtils;

import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * 与snowflake算法区别, 返回字符串id, 占用更多字节, 但直观从id中看出生成时间(主要用于订单号 、 流水号等生成)
 * <p>
 * 注：如果有多个IP的后三位都一样，有可能会产生一样记录
 * <p>
 * 用法：建议只生成一次当前实例
 * IdCodeGenerator idCodeGenerator = new IdCodeGenerator(255, "1");
 * idCodeGenerator.nextId();
 * idCodeGenerator.nextId();
 */
public class Sequence {

    private static final String DATA_FORMAT = "yyMMddHHmmss";

    /**
     * ip（三位IP）
     */
    private final long workerId;
    /**
     * 业务编码号
     */
    private final String businessCode;

    private volatile long sequence = 0L;

    /**
     * 最后三位ip（最大为255）
     */
    private final long workerIdBits = 8L;
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    private final long sequenceBits = 11L;

    private long workerIdShift = sequenceBits;
    private long sequenceMask = -1L ^ (-1L << sequenceBits);

    private final boolean useSystemClock;

    private volatile long lastTimestamp = genTime();

    private static int ip;

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
    public Sequence(String businessCode) {
        this(ip, businessCode, false);
    }

    /**
     * @param businessCode 业务编码号 如：0、1、2
     */
    public Sequence(long workerId, String businessCode) {
        this(workerId, businessCode, false);
    }

    /**
     * @param businessCode 业务编码号 如：0、1、2
     */
    public Sequence(long workerId, String businessCode, boolean useSystemClock) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (StringUtils.isEmpty(businessCode)) {
            throw new IllegalArgumentException("businessCode can't be empty");
        }
        this.useSystemClock = useSystemClock;
        this.workerId = workerId;
        this.businessCode = businessCode;
    }

    public String nextId() {
        long timestamp;
        long seqNum;
        synchronized (this) {
            timestamp = genTime();
            if (timestamp < lastTimestamp) {
                // 如果服务器时间有问题(时钟回拨) 报错。
                throw new IllegalStateException(StrUtil.format("Clock moved backwards. Refusing to generate id for {}ms", lastTimestamp - timestamp));
            }
            if (lastTimestamp == timestamp) {
                sequence = (sequence + 1) & sequenceMask;
                // 如果 sequence 溢出则等待下一个时间。
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
        String datePrefix = DateUtils.formatDate(timestamp, DatePattern.YYMMDDHHMMSSSSS);
        return new StringBuilder(25).append(datePrefix.substring(0, DatePattern.YYMMDDHHMMSS.length())).append(businessCode).append(suffix).append(datePrefix.substring(DatePattern.YYMMDDHHMMSS.length())).toString();}

    /**
     * 循环等待下一个时间
     *
     * @param lastTimestamp 上次记录的时间
     * @return 下一个时间
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = genTime();
        while (timestamp <= lastTimestamp) {
            timestamp = genTime();
        }
        return timestamp;
    }

    private long genTime() {
        return this.useSystemClock ? SystemClock.now() : System.currentTimeMillis();
    }

}