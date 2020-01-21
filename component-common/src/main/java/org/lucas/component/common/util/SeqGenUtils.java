package org.lucas.component.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SeqGenUtils {
    /**
     * 日志ID生成器
     *//*
    private final static IdCodeGenerator LOG_ID_GENERATOR = new IdCodeGenerator(IdCodeTypeEnum.LOG_ID.getCode());

    *//**
     * 活动流水号生成器
     *//*
    private final static IdCodeGenerator ACTIVITY_CODE_GENERATOR = new IdCodeGenerator(IdCodeTypeEnum.ACTIVITY_CODE.getCode());

    *//**
     * 订单号生成器
     *//*
    private final static IdCodeGenerator ORDER_CODE_GENERATOR = new IdCodeGenerator(IdCodeTypeEnum.ORDER_CODE.getCode());

    *//**
     * 转账号生成器
     *//*
    private final static IdCodeGenerator TRANSFER_ACCOUNT_CODE_GENERATOR = new IdCodeGenerator(IdCodeTypeEnum.TRANSFER_ACCOUNT_CODE.getCode());

    *//**
     * 提现号生成器
     *//*
    private final static IdCodeGenerator WITHDRAW_CODE_GENERATOR = new IdCodeGenerator(IdCodeTypeEnum.WITHDRAW_CODE.getCode());


    *//**
     * 促销活动编号生成器
     *//*
    private final static IdCodeGenerator PROMOTION_ACTIVITY_CODE_GENERATOR = new IdCodeGenerator(IdCodeTypeEnum.PROMOTION_ACTIVITY_CODE.getCode());

    *//**
     * 促销商品编号生成器
     *//*
    private final static IdCodeGenerator PROMOTION_PRODUCT_CODE_GENERATOR = new IdCodeGenerator(IdCodeTypeEnum.PROMOTION_PRODUCT_CODE.getCode());

    *//**
     * 财务模块集市币发放流水号
     *//*
    private final static IdCodeGenerator FINANCE_MARKET_CODE_GENERATOR = new IdCodeGenerator(IdCodeTypeEnum.FINANCE_MARKET_CODE.getCode());

    *//**
     * 财务模块佣金发放流水号
     *//*
    private final static IdCodeGenerator FINANCE_COMMISSON_CODE_GENERATOR = new IdCodeGenerator(IdCodeTypeEnum.FINANCE_COMMISSON_CODE.getCode());


    *//**
     * 流水号
     *//*
    private final static IdCodeGenerator SERIAL_CODE_GENERATOR = new IdCodeGenerator(IdCodeTypeEnum.SERIAL_CODE.getCode());


    *//**
     * 获取促销活动编号
     * @return
     *//*
    public static String getPromotionActivityCode(){
        return PROMOTION_ACTIVITY_CODE_GENERATOR.nextId();
    }

    *//**
     * 获取促销商品编号
     * @return
     *//*
    public static String getPromotionProductCode(){
        return PROMOTION_PRODUCT_CODE_GENERATOR.nextId();
    }


    *//**
     * 活动流水号
     *
     * @return
     *//*
    public static String getActivityCode() {
        return ACTIVITY_CODE_GENERATOR.nextId();
    }

    *//**
     * 集市币发放流水号
     *
     * @return
     *//*
    public static String getFinanceMarketCodeCode() {
        return FINANCE_MARKET_CODE_GENERATOR.nextId();
    }

    *//**
     * 佣金发放流水号
     *
     * @return
     *//*
    public static String getFinanceCommissonCode() {
        return FINANCE_COMMISSON_CODE_GENERATOR.nextId();
    }

    *//**
     * 订单号
     *
     * @return
     *//*
    public static String getOrderCode() {
        return ORDER_CODE_GENERATOR.nextId();
    }

    *//**
     * 转账号
     *
     * @return
     *//*
    public static String getTransferAccountCode() {
        return TRANSFER_ACCOUNT_CODE_GENERATOR.nextId();
    }

    *//**
     * 提现号
     *
     * @return
     *//*
    public static String getWithdrawCode() {
        return WITHDRAW_CODE_GENERATOR.nextId();
    }

    *//**
     * 获取流水号(公用)
     * @return
     *//*
    public static String getSerialCode(){
        return SERIAL_CODE_GENERATOR.nextId();
    }

    *//**
     * 主要生成日志ID
     *
     * @return
     *//*
    public static String getLogId() {
        return LOG_ID_GENERATOR.nextId();
    }

    *//**
     * 生成思埠未来集市-订单服务 ID
     *
     * @return
     *//*
    public static String getId() {
        return ObjectId.get(AppName.SIBU_MALL_ORDER).toHexString();
    }

    *//**
     * 生成思埠未来集市-佣金服务 ID
     *
     * @return
     *//*
    public static String getCommissionId() {
        return ObjectId.get(AppName.SIBU_MALL_COMMISSION).toHexString();
    }

    *//**
     * 生成思埠未来集市-拼团/秒杀服务 ID
     *
     * @return
     *//*
    public static String getMarketingId() {
        return ObjectId.get(AppName.SIBU_MALL_MARKETING).toHexString();
    }

    *//**
     * 生成思埠未来集市-财务系统-账户服务 ID
     *
     * @return
     *//*
    public static String getJutebagId() {
        return ObjectId.get(AppName.SIBU_MALL_JUTEBAG).toHexString();
    }


    *//**
     * 判断输入的字符串是否是UUID(包含32位和36位的验证)
     *
     * @param uuid
     * @return
     *//*
    public static boolean isUUID(String uuid) {
        return StringUtil.isUUID(uuid);
    }

    *//**
     * 判断输入的字符串是否是UUID(包含32位和36位的验证)
     *
     * @param uuid
     * @return
     *//*
    public static boolean isNotUUID(String uuid) {
        return StringUtil.isNotUUID(uuid);
    }

    public static String genReqSeq() {
        return DateUtil.getNowTimeYYYYMMddHHMMSS() + DateUtil.getNowTimeYYYYMMddHHMMSS();
    }

    public static void main(String[] args) {
        // String guid = UUID.randomUUID().toString().replace("-", "");
        //log.info(guid);
        System.out.println(getOrderCode());
        String dateSuffix = DateUtil.formatDate(new Date(System.currentTimeMillis()), "yyMMddHHmmssSSS");
        System.out.println(dateSuffix);
        System.out.println(dateSuffix.substring("yyMMddHHmmss".length()));
        //yyMMddHHmmssSSS
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssSSS");
        System.out.println(sdf.format(new Date()));
    }*/
}
