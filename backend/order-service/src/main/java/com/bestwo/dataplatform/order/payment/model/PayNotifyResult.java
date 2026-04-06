package com.bestwo.dataplatform.order.payment.model;

import com.bestwo.dataplatform.order.domain.enums.PayPlatform;
import com.bestwo.dataplatform.order.domain.enums.PaymentOrderStatus;
import java.time.Instant;

public record PayNotifyResult(
    PayPlatform platform,
    String merchantOrderNo,
    String paymentOrderNo,
    String channelOrderNo,
    PaymentOrderStatus status,
    Long amountFen,
    Instant paidAt,
    String notifyId,
    String rawBody,
    String failMessage
) {}
