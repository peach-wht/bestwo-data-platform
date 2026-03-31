package com.bestwo.dataplatform.order.domain.enums;

import com.bestwo.dataplatform.common.exception.BusinessException;

public enum OrderStatus {
    CREATED("CREATED", "订单已创建，尚未发起支付", false),
    WAIT_PAY("WAIT_PAY", "已生成支付单，等待用户支付", false),
    PAYING("PAYING", "支付处理中，等待渠道最终结果", false),
    PAID("PAID", "支付成功", false),
    CLOSED("CLOSED", "订单已关闭", true),
    CANCELLED("CANCELLED", "订单已取消", true),
    REFUNDING("REFUNDING", "退款处理中", false),
    REFUNDED("REFUNDED", "已退款", true);

    private final String code;
    private final String description;
    private final boolean terminal;

    OrderStatus(String code, String description, boolean terminal) {
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

    public static OrderStatus fromCode(String code) {
        for (OrderStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new BusinessException("unsupported order status: " + code);
    }
}
