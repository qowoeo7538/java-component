package org.lucas.component.common.enums;

import org.lucas.component.common.util.EnumUtil;

public enum AppName  {
    DOUBO_SC("DOUBO_SC", "服务中心", "s"),
    DOUBO_BUYER_API("DOUBO_BUYER_API", "买家API", "b"),
    DOUBO_LIVE_API("DOUBO_LIVE_API", "卖家API", "s"),
    DOUBO_ADMIN_WEB("DOUBO_ADMIN_WEB", "后台管理", "w"),
    DOUBO_MQ("DOUBO_MQ", "MQ", "m"),
    DOUBO_CMQ("DOUBO_CMQ", "CMQ", "q"),

    ;


    private String code;


    private String desc;


    private String codeNumber;

    private AppName(String code, String desc, String codeNumber) {
        this.code = code;
        this.desc = desc;
        this.codeNumber = codeNumber;
    }

    public static AppName resolveCodeNumber(String codeNumber) {
        return EnumUtil.fromEnumValue(AppName.class, "codeNumber", codeNumber);
    }

    public static AppName resolve(String code) {
        return EnumUtil.fromEnumValue(AppName.class, "code", code);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCodeNumber() {
        return codeNumber;
    }

    public void setCodeNumber(String codeNumber) {
        this.codeNumber = codeNumber;
    }
}
