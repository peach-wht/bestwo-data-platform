package com.bestwo.dataplatform.order.domain.enums;

import com.bestwo.dataplatform.common.exception.BusinessException;

public enum PayNotifyProcessStatus {
    PENDING("PENDING", "待处理"),
    PROCESSING("PROCESSING", "处理中"),
    PROCESSED("PROCESSED", "已处理"),
    IGNORED("IGNORED", "已忽略"),
    FAILED("FAILED", "处理失败");

    private final String code;
    private final String description;

    PayNotifyProcessStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PayNotifyProcessStatus fromCode(String code) {
        for (PayNotifyProcessStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new BusinessException("unsupported pay notify process status: " + code);
    }
}
