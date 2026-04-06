package com.bestwo.dataplatform.order.payment.spi;

import com.bestwo.dataplatform.common.exception.BusinessException;
import com.bestwo.dataplatform.order.domain.enums.PayPlatform;
import com.bestwo.dataplatform.order.domain.enums.PayTradeType;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PayClientRegistry {

    private final List<PayClient> payClients;

    public PayClientRegistry(List<PayClient> payClients) {
        this.payClients = payClients;
    }

    public PayClient getClient(PayPlatform platform, PayTradeType tradeType) {
        return payClients.stream()
            .filter(client -> client.platform() == platform && client.supports(tradeType))
            .findFirst()
            .orElseThrow(() -> new BusinessException(
                "no pay client found for platform " + platform.getCode() + " and trade type " + tradeType.getCode()
            ));
    }

    public PayClient getClient(PayPlatform platform) {
        return payClients.stream()
            .filter(client -> client.platform() == platform)
            .findFirst()
            .orElseThrow(() -> new BusinessException("no pay client found for platform " + platform.getCode()));
    }
}
