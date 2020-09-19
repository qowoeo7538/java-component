package org.lucas.component.common.util;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.lang.ObjectId;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import org.junit.jupiter.api.Test;
import org.lucas.component.common.core.idcenter.CodeGenerator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.IntStream;

class IdUtilTests {

    /**
     * 获取当前机器编码
     */
    @Test
    void testWorkerId() {
        try {
            long workerId = NetUtil.ipv4ToLong(NetUtil.getLocalhostStr());
            System.out.println("当前机器 workerId:" + workerId);
        } catch (Exception e) {
            System.out.println("获取机器 ID 失败:" + e);
            int workerId = NetUtil.getLocalhost().hashCode();
            System.out.println("当前机器 workerId:" + workerId);
        }
    }

    /**
     * 获取一个批次号，形如 2019071015301361000101237
     * <p>
     * 数据库使用 char(25) 存储
     */
    @Test
    void testBatchId() {
        IntStream.range(1, 10).forEach(i -> System.out.println(batchId(10000, 10)));
    }

    @Test
    void testUidGenerator() {
        CodeGenerator sequence = new CodeGenerator(255, "1");
        IntStream.range(1, 100).forEach(t -> System.out.println(sequence.nextId()));
    }

    /**
     * 生成的是不带-的字符串，类似于：b17f24ff026d40949c85a24f4f375d42
     */
    @Test
    void testSimpleUUID() {
        IntStream.range(1, 10).forEach(i -> System.out.println(IdUtil.simpleUUID()));
    }

    /**
     * 生成的UUID是带-的字符串，类似于：a5c8a5e8-df2b-4706-bea4-08d0939410e3
     */
    @Test
    void testRandomUUID() {
        IntStream.range(1, 10).forEach(i -> System.out.println(IdUtil.randomUUID()));
    }

    /**
     * 生成 Snowflake
     */
    @Test
    void testSnowflake() {
        final long workerId = 0L;
        Snowflake snowflake = IdUtil.createSnowflake(workerId, 1);
        IntStream.range(1, 10).forEach(i -> System.out.println(snowflake.nextId()));
    }

    /**
     * 生成类似：5b9e306a4df4f8c54a39fb0c
     * <p>
     * ObjectId 是 MongoDB 数据库的一种唯一 ID 生成策略，
     * 是 UUID version1 的变种，详细介绍可见：服务化框架－分布式 Unique ID 的生成方法一览。
     */
    @Test
    void testObjectId() {
        IntStream.range(1, 10).forEach(i -> System.out.println(ObjectId.next()));
    }

    /**
     * @param tenantId 租户ID，5 位
     * @param module   业务模块ID，2 位
     * @return 返回批次号
     */
    private synchronized String batchId(int tenantId, int module) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_MS_PATTERN);
        String prefix = dtf.format(LocalDateTime.now());
        return prefix + tenantId + module + RandomUtil.randomNumbers(3);
    }

}
