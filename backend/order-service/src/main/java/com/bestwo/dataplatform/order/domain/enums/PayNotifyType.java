package com.bestwo.dataplatform.order.domain.enums;

import com.bestwo.dataplatform.common.exception.BusinessException;

public enum PayNotifyType {
    PAY("PAY", "支付结果通知"),
    REFUND("REFUND", "退款结果通知"),
    CLOSE("CLOSE", "关单结果通知"),
    UNKNOWN("UNKNOWN", "未知通知类型");

    private final String code;
    private final String description;

    PayNotifyType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PayNotifyType fromCode(String code) {
        for (PayNotifyType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new BusinessException("unsupported pay notify type: " + code);
    }
}
