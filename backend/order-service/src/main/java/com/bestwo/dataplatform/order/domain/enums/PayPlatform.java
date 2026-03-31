package com.bestwo.dataplatform.order.domain.enums;

import com.bestwo.dataplatform.common.exception.BusinessException;

public enum PayPlatform {
    WECHAT_PAY("WECHAT_PAY", "微信支付", true),
    ALIPAY("ALIPAY", "支付宝", true),
    UNION_PAY("UNION_PAY", "银联", true),
    OFFLINE("OFFLINE", "线下收款", false);

    private final String code;
    private final String description;
    private final boolean online;

    PayPlatform(String code, String description, boolean online) {
        this.code = code;
        this.description = description;
        this.online = online;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isOnline() {
        return online;
    }

    public static PayPlatform fromCode(String code) {
        for (PayPlatform platform : values()) {
            if (platform.code.equalsIgnoreCase(code)) {
                return platform;
            }
        }
        throw new BusinessException("unsupported pay platform: " + code);
    }
}
