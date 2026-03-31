package com.bestwo.dataplatform.order.domain.enums;

import com.bestwo.dataplatform.common.exception.BusinessException;

public enum OrderPayStatus {
    UNPAID("UNPAID", "未支付"),
    PAYING("PAYING", "支付处理中"),
    PAID("PAID", "已支付"),
    PART_REFUNDED("PART_REFUNDED", "部分退款"),
    REFUNDING("REFUNDING", "退款处理中"),
    REFUNDED("REFUNDED", "已退款"),
    CLOSED("CLOSED", "已关闭");

    private final String code;
    private final String description;

    OrderPayStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static OrderPayStatus fromCode(String code) {
        for (OrderPayStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new BusinessException("unsupported order pay status: " + code);
    }
}
