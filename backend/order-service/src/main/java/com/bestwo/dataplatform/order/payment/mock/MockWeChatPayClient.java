package com.bestwo.dataplatform.order.payment.mock;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bestwo.dataplatform.common.exception.BusinessException;
import com.bestwo.dataplatform.order.domain.enums.PayPlatform;
import com.bestwo.dataplatform.order.domain.enums.PayTradeType;
import com.bestwo.dataplatform.order.domain.enums.PaymentOrderStatus;
import com.bestwo.dataplatform.order.domain.model.BizPaymentOrder;
import com.bestwo.dataplatform.order.mapper.BizPaymentOrderMapper;
import com.bestwo.dataplatform.order.payment.model.PayNotifyResult;
import com.bestwo.dataplatform.order.payment.model.PayPrepayCommand;
import com.bestwo.dataplatform.order.payment.model.PayPrepayResult;
import com.bestwo.dataplatform.order.payment.model.PayQueryResult;
import com.bestwo.dataplatform.order.payment.spi.PayClient;
import com.bestwo.dataplatform.order.util.OrderNoGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConditionalOnProperty(prefix = "bestwo.pay", name = "provider", havingValue = "MOCK", matchIfMissing = true)
public class MockWeChatPayClient implements PayClient {

    private final BizPaymentOrderMapper bizPaymentOrderMapper;
    private final ObjectMapper objectMapper;

    public MockWeChatPayClient(BizPaymentOrderMapper bizPaymentOrderMapper, ObjectMapper objectMapper) {
        this.bizPaymentOrderMapper = bizPaymentOrderMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public PayPlatform platform() {
        return PayPlatform.WECHAT_PAY;
    }

    @Override
    public boolean supports(PayTradeType tradeType) {
        return tradeType != null;
    }

    @Override
    public PayPrepayResult prepay(PayPrepayCommand command) {
        assertSupports(command.platform(), command.tradeType());

        String mockPayToken = OrderNoGenerator.generateMockPayToken();
        String channelOrderNo = OrderNoGenerator.generateMockChannelOrderNo();
        String mockPayUrl = MockPaymentConstants.buildMockPayUrl(command.paymentOrderNo(), mockPayToken);

        ObjectNode raw = objectMapper.createObjectNode()
            .put("provider", MockPaymentConstants.PROVIDER)
            .put("paymentOrderNo", command.paymentOrderNo())
            .put("orderNo", command.merchantOrderNo())
            .put("tradeType", command.tradeType().getCode())
            .put("mockPayToken", mockPayToken)
            .put("mockPayUrl", mockPayUrl)
            .put("channelOrderNo", channelOrderNo)
            .put("status", PaymentOrderStatus.WAIT_PAY.getCode());

        return new PayPrepayResult(
            PayPlatform.WECHAT_PAY,
            command.tradeType(),
            PaymentOrderStatus.WAIT_PAY,
            command.merchantOrderNo(),
            command.paymentOrderNo(),
            channelOrderNo,
            mockPayUrl,
            mockPayToken,
            command.expireAt(),
            toJson(raw)
        );
    }

    @Override
    public PayQueryResult query(String merchantOrderNo, String paymentOrderNo) {
        BizPaymentOrder paymentOrder = findPaymentOrder(merchantOrderNo, paymentOrderNo);
        if (paymentOrder == null) {
            throw new BusinessException("payment order not found");
        }

        return new PayQueryResult(
            PayPlatform.WECHAT_PAY,
            paymentOrder.getOrderNo(),
            paymentOrder.getPaymentOrderNo(),
            paymentOrder.getChannelOrderNo(),
            paymentOrder.getStatusEnum(),
            paymentOrder.getSuccessAmountFen() != null && paymentOrder.getSuccessAmountFen() > 0
                ? paymentOrder.getSuccessAmountFen()
                : paymentOrder.getRequestAmountFen(),
            paymentOrder.getSuccessTime(),
            paymentOrder.getExtJson()
        );
    }

    @Override
    public PayNotifyResult parseNotify(String requestBody, Map<String, String> headers) {
        JsonNode payload = readJson(requestBody);
        String tradeState = text(payload, "tradeState");
        return new PayNotifyResult(
            PayPlatform.WECHAT_PAY,
            firstNonBlank(text(payload, "orderNo"), text(payload, "order_no")),
            firstNonBlank(text(payload, "paymentOrderNo"), text(payload, "payment_order_no")),
            firstNonBlank(text(payload, "channelOrderNo"), text(payload, "channel_order_no")),
            mapTradeState(tradeState),
            amountFromJson(payload.path("amount")),
            parseTime(firstNonBlank(text(payload, "successTime"), text(payload, "paidAt"))),
            firstNonBlank(text(payload, "notifyNo"), text(payload, "notifyId"), text(payload, "notify_id")),
            requestBody,
            firstNonBlank(text(payload, "reason"), text(payload, "failMessage"))
        );
    }

    private BizPaymentOrder findPaymentOrder(String merchantOrderNo, String paymentOrderNo) {
        if (StringUtils.hasText(paymentOrderNo)) {
            BizPaymentOrder paymentOrder = bizPaymentOrderMapper.selectOne(
                new LambdaQueryWrapper<BizPaymentOrder>()
                    .eq(BizPaymentOrder::getPaymentOrderNo, paymentOrderNo)
                    .last("LIMIT 1")
            );
            if (paymentOrder != null) {
                return paymentOrder;
            }
        }

        if (!StringUtils.hasText(merchantOrderNo)) {
            return null;
        }

        return bizPaymentOrderMapper.selectOne(
            new LambdaQueryWrapper<BizPaymentOrder>()
                .eq(BizPaymentOrder::getOrderNo, merchantOrderNo)
                .orderByDesc(BizPaymentOrder::getCreatedAt)
                .last("LIMIT 1")
        );
    }

    private JsonNode readJson(String requestBody) {
        try {
            return objectMapper.readTree(requestBody);
        } catch (JsonProcessingException exception) {
            throw new BusinessException("invalid mock payment notify body");
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException("failed to serialize mock pay response");
        }
    }

    private String text(JsonNode node, String fieldName) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        JsonNode field = node.path(fieldName);
        return field.isMissingNode() || field.isNull() ? null : field.asText();
    }

    private Long amountFromJson(JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull() ? null : node.asLong();
    }

    private Instant parseTime(String value) {
        return StringUtils.hasText(value) ? Instant.parse(value) : null;
    }

    private PaymentOrderStatus mapTradeState(String tradeState) {
        if (!StringUtils.hasText(tradeState)) {
            return PaymentOrderStatus.WAIT_PAY;
        }

        return switch (tradeState.trim().toUpperCase()) {
            case "SUCCESS" -> PaymentOrderStatus.SUCCESS;
            case "FAILED", "FAIL" -> PaymentOrderStatus.FAILED;
            case "CLOSED" -> PaymentOrderStatus.CLOSED;
            default -> PaymentOrderStatus.WAIT_PAY;
        };
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }
}
