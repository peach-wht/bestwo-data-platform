package com.bestwo.dataplatform.order.payment.model;

import com.bestwo.dataplatform.common.exception.BusinessException;
import com.bestwo.dataplatform.order.domain.enums.PayPlatform;
import com.bestwo.dataplatform.order.domain.enums.PayTradeType;
import java.time.Instant;

public record PayPrepayCommand(
    String merchantOrderNo,
    String paymentOrderNo,
    PayPlatform platform,
    PayTradeType tradeType,
    String subject,
    String description,
    Long amountFen,
    String currency,
    String notifyUrl,
    String payerId,
    Instant expireAt,
    String clientIp
) {
    public PayPrepayCommand {
        if (merchantOrderNo == null || merchantOrderNo.isBlank()) {
            throw new BusinessException("merchantOrderNo must not be blank");
        }
        if (paymentOrderNo == null || paymentOrderNo.isBlank()) {
            throw new BusinessException("paymentOrderNo must not be blank");
        }
        if (platform == null) {
            throw new BusinessException("platform must not be null");
        }
        if (tradeType == null) {
            throw new BusinessException("tradeType must not be null");
        }
        if (subject == null || subject.isBlank()) {
            throw new BusinessException("subject must not be blank");
        }
        if (amountFen == null || amountFen <= 0) {
            throw new BusinessException("amountFen must be greater than zero");
        }
        if (notifyUrl == null || notifyUrl.isBlank()) {
            throw new BusinessException("notifyUrl must not be blank");
        }
    }
}
