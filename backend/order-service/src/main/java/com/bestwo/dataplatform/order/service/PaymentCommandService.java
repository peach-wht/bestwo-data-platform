package com.bestwo.dataplatform.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bestwo.dataplatform.common.exception.BusinessException;
import com.bestwo.dataplatform.order.config.PayProperties;
import com.bestwo.dataplatform.order.domain.enums.OrderPayStatus;
import com.bestwo.dataplatform.order.domain.enums.OrderStatus;
import com.bestwo.dataplatform.order.domain.enums.PayPlatform;
import com.bestwo.dataplatform.order.domain.enums.PayTradeType;
import com.bestwo.dataplatform.order.domain.enums.PaymentOrderStatus;
import com.bestwo.dataplatform.order.domain.model.BizOrder;
import com.bestwo.dataplatform.order.domain.model.BizPaymentOrder;
import com.bestwo.dataplatform.order.domain.support.OrderStatusFlow;
import com.bestwo.dataplatform.order.dto.OrderPrepayResponse;
import com.bestwo.dataplatform.order.mapper.BizOrderMapper;
import com.bestwo.dataplatform.order.mapper.BizPaymentOrderMapper;
import com.bestwo.dataplatform.order.payment.mock.MockPaymentConstants;
import com.bestwo.dataplatform.order.payment.model.PayPrepayCommand;
import com.bestwo.dataplatform.order.payment.model.PayPrepayResult;
import com.bestwo.dataplatform.order.payment.spi.PayClient;
import com.bestwo.dataplatform.order.payment.spi.PayClientRegistry;
import com.bestwo.dataplatform.order.payment.wechat.WeChatPayProperties;
import com.bestwo.dataplatform.order.util.OrderNoGenerator;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

@Service
public class PaymentCommandService {

    private static final String DEFAULT_CURRENCY = "CNY";
    private static final String DEFAULT_OPERATOR = "system";
    private static final long DEFAULT_EXPIRE_MINUTES = 120;

    private final BizOrderMapper bizOrderMapper;
    private final BizPaymentOrderMapper bizPaymentOrderMapper;
    private final PayClientRegistry payClientRegistry;
    private final PayProperties payProperties;
    private final WeChatPayProperties weChatPayProperties;
    private final TransactionTemplate transactionTemplate;

    public PaymentCommandService(
        BizOrderMapper bizOrderMapper,
        BizPaymentOrderMapper bizPaymentOrderMapper,
        PayClientRegistry payClientRegistry,
        PayProperties payProperties,
        WeChatPayProperties weChatPayProperties,
        TransactionTemplate transactionTemplate
    ) {
        this.bizOrderMapper = bizOrderMapper;
        this.bizPaymentOrderMapper = bizPaymentOrderMapper;
        this.payClientRegistry = payClientRegistry;
        this.payProperties = payProperties;
        this.weChatPayProperties = weChatPayProperties;
        this.transactionTemplate = transactionTemplate;
    }

    public OrderPrepayResponse prepay(String orderIdOrOrderNo, String clientIp) {
        BizOrder order = findOrder(orderIdOrOrderNo);
        if (order == null) {
            throw new BusinessException("order not found");
        }

        assertOrderCanPrepay(order);

        PayPlatform platform = PayPlatform.fromCode(order.getPreferredPayPlatform());
        PayTradeType tradeType = PayTradeType.fromCode(order.getPreferredTradeType());
        PayClient payClient = payClientRegistry.getClient(platform, tradeType);

        BizPaymentOrder activePaymentOrder = findActivePaymentOrder(order.getOrderId(), platform, tradeType);
        if (activePaymentOrder != null) {
            return toOrderPrepayResponse(order, activePaymentOrder);
        }

        BizPaymentOrder paymentOrder = createPrepayingPaymentOrder(order, platform, tradeType, clientIp);
        PayPrepayCommand command = buildPrepayCommand(order, paymentOrder);

        try {
            PayPrepayResult prepayResult = payClient.prepay(command);
            return finalizePrepaySuccess(order, paymentOrder, prepayResult);
        } catch (RuntimeException exception) {
            markPaymentOrderFailed(paymentOrder, exception.getMessage());
            throw exception;
        }
    }

    private BizOrder findOrder(String orderIdOrOrderNo) {
        String keyword = orderIdOrOrderNo == null ? "" : orderIdOrOrderNo.trim();
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException("order id is required");
        }

        LambdaQueryWrapper<BizOrder> wrapper = new LambdaQueryWrapper<BizOrder>()
            .eq(BizOrder::getOrderId, keyword)
            .or()
            .eq(BizOrder::getOrderNo, keyword);

        if (keyword.chars().allMatch(Character::isDigit)) {
            wrapper.or().eq(BizOrder::getId, Long.parseLong(keyword));
        }

        return bizOrderMapper.selectOne(wrapper.last("LIMIT 1"));
    }

    private void assertOrderCanPrepay(BizOrder order) {
        OrderStatus currentOrderStatus = order.getOrderStatusEnum();
        if (currentOrderStatus == OrderStatus.PAID
            || currentOrderStatus == OrderStatus.CLOSED
            || currentOrderStatus == OrderStatus.CANCELLED
            || currentOrderStatus == OrderStatus.REFUNDING
            || currentOrderStatus == OrderStatus.REFUNDED) {
            throw new BusinessException("current order status does not allow prepay: " + currentOrderStatus.getCode());
        }

        OrderPayStatus currentPayStatus = order.getPayStatusEnum();
        if (currentPayStatus == OrderPayStatus.PAID
            || currentPayStatus == OrderPayStatus.PART_REFUNDED
            || currentPayStatus == OrderPayStatus.REFUNDING
            || currentPayStatus == OrderPayStatus.REFUNDED
            || currentPayStatus == OrderPayStatus.CLOSED) {
            throw new BusinessException("current pay status does not allow prepay: " + currentPayStatus.getCode());
        }
    }

    private BizPaymentOrder findActivePaymentOrder(String orderId, PayPlatform platform, PayTradeType tradeType) {
        BizPaymentOrder paymentOrder = bizPaymentOrderMapper.selectOne(
            new LambdaQueryWrapper<BizPaymentOrder>()
                .eq(BizPaymentOrder::getOrderId, orderId)
                .eq(BizPaymentOrder::getPlatform, platform.getCode())
                .eq(BizPaymentOrder::getTradeType, tradeType.getCode())
                .eq(BizPaymentOrder::getStatus, PaymentOrderStatus.WAIT_PAY.getCode())
                .orderByDesc(BizPaymentOrder::getCreatedAt)
                .last("LIMIT 1")
        );
        if (paymentOrder == null || !StringUtils.hasText(paymentOrder.getCodeUrl())) {
            return null;
        }
        return paymentOrder.getExpiredTime() == null || paymentOrder.getExpiredTime().isAfter(Instant.now())
            ? paymentOrder
            : null;
    }

    private BizPaymentOrder createPrepayingPaymentOrder(
        BizOrder order,
        PayPlatform platform,
        PayTradeType tradeType,
        String clientIp
    ) {
        BizPaymentOrder paymentOrder = new BizPaymentOrder();
        paymentOrder.setPaymentOrderId(OrderNoGenerator.generatePaymentOrderId());
        paymentOrder.setPaymentOrderNo(generateUniquePaymentOrderNo());
        paymentOrder.setOrderId(order.getOrderId());
        paymentOrder.setOrderNo(order.getOrderNo());
        paymentOrder.setPlatform(platform.getCode());
        paymentOrder.setTradeType(tradeType.getCode());
        paymentOrder.setMerchantCode(resolveMerchantCode(platform));
        paymentOrder.setChannelAppId(resolveChannelAppId(platform));
        paymentOrder.setChannelMerchantId(resolveChannelMerchantId(platform));
        paymentOrder.setSubject(order.getOrderTitle());
        paymentOrder.setDescription(order.getOrderDescription());
        paymentOrder.setCurrency(StringUtils.hasText(order.getCurrency()) ? order.getCurrency() : DEFAULT_CURRENCY);
        paymentOrder.setRequestAmountFen(order.getPayableAmountFen());
        paymentOrder.setSuccessAmountFen(0L);
        paymentOrder.setPayerId(trimToNull(order.getBuyerId()));
        paymentOrder.setClientIp(trimToNull(clientIp));
        paymentOrder.setNotifyUrl(resolveNotifyUrl(platform));
        paymentOrder.setStatus(PaymentOrderStatus.PREPAYING.getCode());
        paymentOrder.setVersion(0);
        paymentOrder.setCreatedBy(DEFAULT_OPERATOR);
        paymentOrder.setUpdatedBy(DEFAULT_OPERATOR);

        Instant now = Instant.now();
        paymentOrder.setCreatedAt(now);
        paymentOrder.setUpdatedAt(now);
        paymentOrder.setExpiredTime(now.plus(DEFAULT_EXPIRE_MINUTES, ChronoUnit.MINUTES));

        int affectedRows = bizPaymentOrderMapper.insert(paymentOrder);
        if (affectedRows != 1) {
            throw new BusinessException("failed to create payment order");
        }
        return paymentOrder;
    }

    private PayPrepayCommand buildPrepayCommand(BizOrder order, BizPaymentOrder paymentOrder) {
        return new PayPrepayCommand(
            order.getOrderNo(),
            paymentOrder.getPaymentOrderNo(),
            paymentOrder.getPlatformEnum(),
            paymentOrder.getTradeTypeEnum(),
            paymentOrder.getSubject(),
            paymentOrder.getDescription(),
            paymentOrder.getRequestAmountFen(),
            paymentOrder.getCurrency(),
            paymentOrder.getNotifyUrl(),
            paymentOrder.getPayerId(),
            paymentOrder.getExpiredTime(),
            paymentOrder.getClientIp()
        );
    }

    private OrderPrepayResponse finalizePrepaySuccess(
        BizOrder order,
        BizPaymentOrder paymentOrder,
        PayPrepayResult prepayResult
    ) {
        OrderPrepayResponse response = transactionTemplate.execute(status -> {
            applyPrepaySuccess(paymentOrder, prepayResult);
            updateOrderAfterPrepay(order, paymentOrder);
            return toOrderPrepayResponse(order, paymentOrder);
        });

        if (response == null) {
            throw new BusinessException("failed to finalize payment prepay");
        }
        return response;
    }

    private void applyPrepaySuccess(BizPaymentOrder paymentOrder, PayPrepayResult prepayResult) {
        PaymentOrderStatus currentStatus = paymentOrder.getStatusEnum();
        PaymentOrderStatus nextStatus = prepayResult.status();
        if (currentStatus != nextStatus) {
            com.bestwo.dataplatform.order.domain.support.PaymentOrderStatusFlow.assertCanTransition(currentStatus, nextStatus);
        }

        paymentOrder.setStatus(nextStatus.getCode());
        paymentOrder.setChannelOrderNo(trimToNull(prepayResult.channelOrderNo()));
        paymentOrder.setChannelPrepayId(trimToNull(prepayResult.prepayToken()));
        paymentOrder.setCodeUrl(trimToNull(prepayResult.codeUrl()));
        paymentOrder.setExpiredTime(prepayResult.expireAt() == null ? paymentOrder.getExpiredTime() : prepayResult.expireAt());
        paymentOrder.setFailCode(null);
        paymentOrder.setFailMessage(null);
        paymentOrder.setExtJson(prepayResult.rawResponseBody());
        paymentOrder.setUpdatedBy(DEFAULT_OPERATOR);
        paymentOrder.setUpdatedAt(Instant.now());

        int affectedRows = bizPaymentOrderMapper.updateById(paymentOrder);
        if (affectedRows != 1) {
            throw new BusinessException("failed to update payment order prepay result");
        }
    }

    private void updateOrderAfterPrepay(BizOrder order, BizPaymentOrder paymentOrder) {
        OrderStatus currentOrderStatus = order.getOrderStatusEnum();
        if (currentOrderStatus != OrderStatus.WAIT_PAY) {
            OrderStatusFlow.assertCanTransition(currentOrderStatus, OrderStatus.WAIT_PAY);
            order.setOrderStatus(OrderStatus.WAIT_PAY.getCode());
        }
        order.setPayStatus(OrderPayStatus.UNPAID.getCode());
        order.setLatestPaymentOrderNo(paymentOrder.getPaymentOrderNo());
        order.setLatestChannelOrderNo(paymentOrder.getChannelOrderNo());
        order.setExpiredTime(paymentOrder.getExpiredTime());
        order.setUpdatedBy(DEFAULT_OPERATOR);
        order.setUpdatedAt(Instant.now());

        int affectedRows = bizOrderMapper.updateById(order);
        if (affectedRows != 1) {
            throw new BusinessException("failed to update order prepay status");
        }
    }

    private void markPaymentOrderFailed(BizPaymentOrder paymentOrder, String errorMessage) {
        PaymentOrderStatus currentStatus = paymentOrder.getStatusEnum();
        if (currentStatus != PaymentOrderStatus.FAILED) {
            com.bestwo.dataplatform.order.domain.support.PaymentOrderStatusFlow.assertCanTransition(
                currentStatus,
                PaymentOrderStatus.FAILED
            );
        }

        paymentOrder.setStatus(PaymentOrderStatus.FAILED.getCode());
        paymentOrder.setFailCode("PREPAY_ERROR");
        paymentOrder.setFailMessage(truncate(errorMessage, 255));
        paymentOrder.setUpdatedBy(DEFAULT_OPERATOR);
        paymentOrder.setUpdatedAt(Instant.now());
        bizPaymentOrderMapper.updateById(paymentOrder);
    }

    private OrderPrepayResponse toOrderPrepayResponse(BizOrder order, BizPaymentOrder paymentOrder) {
        OrderPrepayResponse response = new OrderPrepayResponse();
        response.setOrderId(order.getOrderId());
        response.setOrderNo(order.getOrderNo());
        response.setPaymentOrderId(paymentOrder.getPaymentOrderId());
        response.setPaymentOrderNo(paymentOrder.getPaymentOrderNo());
        response.setPlatform(paymentOrder.getPlatform());
        response.setTradeType(paymentOrder.getTradeType());
        response.setStatus(paymentOrder.getStatus());
        response.setCodeUrl(paymentOrder.getCodeUrl());
        response.setPaymentProvider(resolvePaymentProvider(paymentOrder));
        response.setMockMode(isMockPaymentOrder(paymentOrder));
        response.setMockPayToken(isMockPaymentOrder(paymentOrder) ? paymentOrder.getChannelPrepayId() : null);
        response.setMockPayUrl(isMockPaymentOrder(paymentOrder) ? paymentOrder.getCodeUrl() : null);
        response.setChannelOrderNo(paymentOrder.getChannelOrderNo());
        response.setChannelPrepayId(paymentOrder.getChannelPrepayId());
        response.setExpireAt(paymentOrder.getExpiredTime());
        response.setCreatedAt(paymentOrder.getCreatedAt());
        return response;
    }

    private String generateUniquePaymentOrderNo() {
        for (int i = 0; i < 5; i++) {
            String candidate = OrderNoGenerator.generatePaymentOrderNo();
            Long count = bizPaymentOrderMapper.selectCount(
                new LambdaQueryWrapper<BizPaymentOrder>().eq(BizPaymentOrder::getPaymentOrderNo, candidate)
            );
            if (count == null || count == 0) {
                return candidate;
            }
        }
        throw new BusinessException("failed to generate unique payment order number");
    }

    private String resolveChannelAppId(PayPlatform platform) {
        if (isMockPlatform(platform)) {
            return null;
        }
        return platform == PayPlatform.WECHAT_PAY ? trimToNull(weChatPayProperties.getAppId()) : null;
    }

    private String resolveChannelMerchantId(PayPlatform platform) {
        if (isMockPlatform(platform)) {
            return null;
        }
        return platform == PayPlatform.WECHAT_PAY ? trimToNull(weChatPayProperties.getMerchantId()) : null;
    }

    private String resolveNotifyUrl(PayPlatform platform) {
        if (isMockPlatform(platform)) {
            return MockPaymentConstants.NOTIFY_URL;
        }
        return platform == PayPlatform.WECHAT_PAY ? trimToNull(weChatPayProperties.getNotifyUrl()) : null;
    }

    private String resolveMerchantCode(PayPlatform platform) {
        if (isMockPlatform(platform)) {
            return MockPaymentConstants.MERCHANT_CODE;
        }
        return platform.getCode();
    }

    private boolean isMockPlatform(PayPlatform platform) {
        return platform == PayPlatform.WECHAT_PAY && payProperties.isMockProvider();
    }

    private boolean isMockPaymentOrder(BizPaymentOrder paymentOrder) {
        return MockPaymentConstants.MERCHANT_CODE.equalsIgnoreCase(paymentOrder.getMerchantCode());
    }

    private String resolvePaymentProvider(BizPaymentOrder paymentOrder) {
        return isMockPaymentOrder(paymentOrder) ? MockPaymentConstants.PROVIDER : "WECHAT";
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
