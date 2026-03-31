package com.bestwo.dataplatform.order.payment.model;

import com.bestwo.dataplatform.order.domain.enums.PayPlatform;
import com.bestwo.dataplatform.order.domain.enums.PaymentOrderStatus;
import java.time.Instant;

public record PayQueryResult(
    PayPlatform platform,
    String merchantOrderNo,
    String paymentOrderNo,
    String channelOrderNo,
    PaymentOrderStatus status,
    Long amountFen,
    Instant paidAt,
    String rawResponseBody
) {}
