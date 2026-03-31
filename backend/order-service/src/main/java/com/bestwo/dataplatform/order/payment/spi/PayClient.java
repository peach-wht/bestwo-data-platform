package com.bestwo.dataplatform.order.payment.spi;

import com.bestwo.dataplatform.common.exception.BusinessException;
import com.bestwo.dataplatform.order.domain.enums.PayPlatform;
import com.bestwo.dataplatform.order.domain.enums.PayTradeType;
import com.bestwo.dataplatform.order.payment.model.PayNotifyResult;
import com.bestwo.dataplatform.order.payment.model.PayPrepayCommand;
import com.bestwo.dataplatform.order.payment.model.PayPrepayResult;
import com.bestwo.dataplatform.order.payment.model.PayQueryResult;
import java.util.Map;

public interface PayClient {

    PayPlatform platform();

    boolean supports(PayTradeType tradeType);

    PayPrepayResult prepay(PayPrepayCommand command);

    PayQueryResult query(String merchantOrderNo, String paymentOrderNo);

    PayNotifyResult parseNotify(String requestBody, Map<String, String> headers);

    default void assertSupports(PayPlatform payPlatform, PayTradeType tradeType) {
        if (platform() != payPlatform) {
            throw new BusinessException("pay client does not support platform: " + payPlatform);
        }
        if (!supports(tradeType)) {
            throw new BusinessException("pay client does not support trade type: " + tradeType);
        }
    }
}
