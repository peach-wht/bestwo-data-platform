package com.bestwo.dataplatform.order.domain.enums;

import com.bestwo.dataplatform.common.exception.BusinessException;

public enum PaymentOrderStatus {
    INIT("INIT", "支付单已创建，尚未调用支付渠道", false),
    PREPAYING("PREPAYING", "正在请求渠道预下单", false),
    WAIT_PAY("WAIT_PAY", "渠道单已生成，等待用户支付", false),
    SUCCESS("SUCCESS", "支付成功", false),
    CLOSED("CLOSED", "支付单已关闭", true),
    FAILED("FAILED", "支付失败", true),
    REFUNDING("REFUNDING", "退款处理中", false),
    REFUNDED("REFUNDED", "已退款", true);

    private final String code;
    private final String description;
    private final boolean terminal;

    PaymentOrderStatus(String code, String description, boolean terminal) {
        this.code = code;
        this.description = description;
        this.terminal = terminal;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTerminal() {
        return terminal;
    }

    public static PaymentOrderStatus fromCode(String code) {
        for (PaymentOrderStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new BusinessException("unsupported payment order status: " + code);
    }
}
