package com.bestwo.dataplatform.order.domain.enums;

import com.bestwo.dataplatform.common.exception.BusinessException;

public enum PayTradeType {
    NATIVE("NATIVE", "Native 扫码支付"),
    JSAPI("JSAPI", "公众号/服务号支付"),
    MINI_PROGRAM("MINI_PROGRAM", "小程序支付"),
    H5("H5", "H5 支付"),
    APP("APP", "App 支付"),
    BARCODE("BARCODE", "条码/刷卡支付");

    private final String code;
    private final String description;

    PayTradeType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PayTradeType fromCode(String code) {
        for (PayTradeType tradeType : values()) {
            if (tradeType.code.equalsIgnoreCase(code)) {
                return tradeType;
            }
        }
        throw new BusinessException("unsupported pay trade type: " + code);
    }
}
