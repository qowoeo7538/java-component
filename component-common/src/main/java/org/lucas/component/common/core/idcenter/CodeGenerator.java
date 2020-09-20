package org.lucas.component.common.core.idcenter;

import cn.hutool.core.date.SystemClock;
import cn.hutool.core.util.StrUtil;
import org.lucas.component.common.date.DatePattern;
import org.lucas.component.common.date.DateUtils;
import org.lucas.component.common.util.NetUtils;
import org.springframework.util.StringUtils;

import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * 与snowflake算法区别, 返回字符串id, 占用更多字节, 但直观从id中看出生成时间(主要用于订单号 、 流水号等生成)
 * 编码构成： YYMMDDHHMMSS(年月日字符)-> businessCode(业务编码字符)-> workerId(8 bit)-> 自增序列(11 bit)-> SSS(毫秒字符)
 * <p>
 * 注：如果有多个IP的后三位都一样，有可能会产生一样记录
 * <p>
 * 用法：建议只生成一次当前实例
 * IdCodeGenerator idCodeGenerator = new IdCodeGenerator(255, "1");
 * idCodeGenerator.nextId();
 * idCodeGenerator.nextId();
 */
public class CodeGenerator {

    /**
     * workerId（默认为后三位IP地址）
     */
    private final long workerId;
    /**
     * 业务编码号
     */
    private final String businessCode;

    /**
     * IP
     */
    private static final int IP;

    /**
     * 初始自增序列
     */
    private long sequence = 0L;

    /**
     * workerId bits 最后三位ip（最大为255）
     */
    private static final long WORKER_ID_BITS = 8L;

    /**
     * 自增 bits
     */
    private static final long SEQUENCE_BITS = 11L;


    /**
     * workerId 最大值为255
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /**
     * 自增序列最大值为 2047
     */
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    /**
     * workerId 偏移量
     */
    private static long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /**
     * 是否使用系统时间
     */
    private final boolean useSystemClock;

    /**
     * 最后生成时间
     */
    private volatile long lastTimestamp = genTime();

    static {
        try {
            String ipStr = NetUtils.getLocalIp().substring(NetUtils.getLocalIp().length() - 3, NetUtils.getLocalIp().length());
            IP = Integer.parseInt(ipStr);
        } catch (final SocketException | UnknownHostException e) {
            throw new IllegalArgumentException("");
        }
    }

    /**
     * @param businessCode 业务编码号 如：0、1、2
     */
    public CodeGenerator(String businessCode) {
        this(IP, businessCode, false);
    }

    /**
     * @param businessCode 业务编码号 如：0、1、2
     */
    public CodeGenerator(long workerId, String businessCode) {
        this(workerId, businessCode, false);
    }

    /**
     * @param businessCode 业务编码号 如：0、1、2
     */
    public CodeGenerator(long workerId, String businessCode, boolean useSystemClock) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", MAX_WORKER_ID));
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
        // 1 保证线程安全
        synchronized (this) {
            timestamp = genTime();
            // 1.2 如果服务器时间有问题(时钟回拨) 报错。
            if (timestamp < lastTimestamp) {
                throw new IllegalStateException(StrUtil.format("Clock moved backwards. Refusing to generate id for {}ms", lastTimestamp - timestamp));
            }
            // 1.3 如果当前时间和上一次是同一秒时间
            if (lastTimestamp == timestamp) {
                // 1.3.1 sequence自增。
                sequence = (sequence + 1) & MAX_SEQUENCE;
                // 1.3.2 如果 sequence 溢出则等待下一个时间。
                if (sequence == 0) {
                    timestamp = tilNextMillis(lastTimestamp);
                }
            } else {
                // 1.4 如果是新的一秒，那么sequence重新从0开始
                sequence = 0L;
            }
            seqNum = sequence;
            lastTimestamp = timestamp;
        }
        long suffix = (workerId << WORKER_ID_SHIFT) | seqNum;
        String datePrefix = DateUtils.formatDate(timestamp, DatePattern.YYMMDDHHMMSSSSS);

        return datePrefix.substring(0, DatePattern.YYMMDDHHMMSS.length()) + businessCode + suffix
                + datePrefix.substring(DatePattern.YYMMDDHHMMSS.length());
    }

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