package com.bestwo.dataplatform.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bestwo.dataplatform.common.exception.BusinessException;
import com.bestwo.dataplatform.order.config.PayProperties;
import com.bestwo.dataplatform.order.domain.model.BizPaymentOrder;
import com.bestwo.dataplatform.order.dto.MockPaymentActionRequest;
import com.bestwo.dataplatform.order.dto.MockPaymentQueryResponse;
import com.bestwo.dataplatform.order.mapper.BizPaymentOrderMapper;
import com.bestwo.dataplatform.order.payment.mock.MockPaymentConstants;
import com.bestwo.dataplatform.order.util.OrderNoGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MockPaymentService {

    private static final String DEFAULT_OPERATOR = "mock-user";
    private static final String DEFAULT_FAIL_REASON = "USER_CANCEL";

    private final BizPaymentOrderMapper bizPaymentOrderMapper;
    private final PaymentNotifyService paymentNotifyService;
    private final PayProperties payProperties;
    private final ObjectMapper objectMapper;

    public MockPaymentService(
        BizPaymentOrderMapper bizPaymentOrderMapper,
        PaymentNotifyService paymentNotifyService,
        PayProperties payProperties,
        ObjectMapper objectMapper
    ) {
        this.bizPaymentOrderMapper = bizPaymentOrderMapper;
        this.paymentNotifyService = paymentNotifyService;
        this.payProperties = payProperties;
        this.objectMapper = objectMapper;
    }

    public MockPaymentQueryResponse getMockPayment(String paymentOrderNo) {
        payProperties.assertMockProvider();
        return toResponse(findMockPaymentOrder(paymentOrderNo));
    }

    public MockPaymentQueryResponse mockSuccess(String paymentOrderNo, MockPaymentActionRequest request) {
        BizPaymentOrder paymentOrder = findMockPaymentOrder(paymentOrderNo);
        paymentNotifyService.handleMockPayNotify(buildNotifyPayload(paymentOrder, "SUCCESS", request));
        return toResponse(findMockPaymentOrder(paymentOrderNo));
    }

    public MockPaymentQueryResponse mockFail(String paymentOrderNo, MockPaymentActionRequest request) {
        BizPaymentOrder paymentOrder = findMockPaymentOrder(paymentOrderNo);
        paymentNotifyService.handleMockPayNotify(buildNotifyPayload(paymentOrder, "FAILED", request));
        return toResponse(findMockPaymentOrder(paymentOrderNo));
    }

    public void handleMockNotify(String requestBody) {
        payProperties.assertMockProvider();
        paymentNotifyService.handleMockPayNotify(requestBody);
    }

    private BizPaymentOrder findMockPaymentOrder(String paymentOrderNo) {
        payProperties.assertMockProvider();

        if (!StringUtils.hasText(paymentOrderNo)) {
            throw new BusinessException("payment order no is required");
        }

        BizPaymentOrder paymentOrder = bizPaymentOrderMapper.selectOne(
            new LambdaQueryWrapper<BizPaymentOrder>()
                .eq(BizPaymentOrder::getPaymentOrderNo, paymentOrderNo.trim())
                .last("LIMIT 1")
        );
        if (paymentOrder == null) {
            throw new BusinessException("payment order not found");
        }
        if (!isMockPaymentOrder(paymentOrder)) {
            throw new BusinessException("current payment order is not in mock mode");
        }
        return paymentOrder;
    }

    private boolean isMockPaymentOrder(BizPaymentOrder paymentOrder) {
        return MockPaymentConstants.MERCHANT_CODE.equalsIgnoreCase(paymentOrder.getMerchantCode())
            || (paymentOrder.getChannelPrepayId() != null && paymentOrder.getChannelPrepayId().startsWith("mock_"));
    }

    private String buildNotifyPayload(BizPaymentOrder paymentOrder, String tradeState, MockPaymentActionRequest request) {
        String operator = request == null || !StringUtils.hasText(request.getOperator())
            ? DEFAULT_OPERATOR
            : request.getOperator().trim();
        String reason = request == null || !StringUtils.hasText(request.getReason())
            ? DEFAULT_FAIL_REASON
            : request.getReason().trim();

        ObjectNode payload = objectMapper.createObjectNode()
            .put("provider", MockPaymentConstants.PROVIDER)
            .put("notifyNo", OrderNoGenerator.generateNotifyLogId())
            .put("paymentOrderNo", paymentOrder.getPaymentOrderNo())
            .put("orderNo", paymentOrder.getOrderNo())
            .put("tradeState", tradeState)
            .put("amount", paymentOrder.getRequestAmountFen() == null ? 0L : paymentOrder.getRequestAmountFen())
            .put("channel", MockPaymentConstants.MERCHANT_CODE)
            .put("operator", operator)
            .put("mockPayToken", paymentOrder.getChannelPrepayId() == null ? "" : paymentOrder.getChannelPrepayId())
            .put("channelOrderNo", paymentOrder.getChannelOrderNo() == null ? "" : paymentOrder.getChannelOrderNo());

        if ("SUCCESS".equalsIgnoreCase(tradeState)) {
            payload.put("successTime", Instant.now().toString());
        } else {
            payload.put("reason", reason);
        }

        return toJson(payload);
    }

    private MockPaymentQueryResponse toResponse(BizPaymentOrder paymentOrder) {
        MockPaymentQueryResponse response = new MockPaymentQueryResponse();
        response.setPaymentOrderNo(paymentOrder.getPaymentOrderNo());
        response.setOrderNo(paymentOrder.getOrderNo());
        response.setStatus(paymentOrder.getStatus());
        response.setPlatform(paymentOrder.getPlatform());
        response.setTradeType(paymentOrder.getTradeType());
        response.setPaymentProvider(MockPaymentConstants.PROVIDER);
        response.setMockMode(true);
        response.setMockPayToken(paymentOrder.getChannelPrepayId());
        response.setMockPayUrl(paymentOrder.getCodeUrl());
        response.setChannelOrderNo(paymentOrder.getChannelOrderNo());
        response.setSuccessTime(paymentOrder.getSuccessTime());
        response.setFailMessage(paymentOrder.getFailMessage());
        return response;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException("failed to build mock notify payload");
        }
    }
}
