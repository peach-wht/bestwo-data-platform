package com.bestwo.dataplatform.order.payment.model;

import com.bestwo.dataplatform.order.domain.enums.PayPlatform;
import com.bestwo.dataplatform.order.domain.enums.PayTradeType;
import com.bestwo.dataplatform.order.domain.enums.PaymentOrderStatus;
import java.time.Instant;

public record PayPrepayResult(
    PayPlatform platform,
    PayTradeType tradeType,
    PaymentOrderStatus status,
    String merchantOrderNo,
    String paymentOrderNo,
    String channelOrderNo,
    String codeUrl,
    String prepayToken,
    Instant expireAt,
    String rawResponseBody
) {}
