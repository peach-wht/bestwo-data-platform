package com.bestwo.dataplatform.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bestwo.dataplatform.common.exception.BusinessException;
import com.bestwo.dataplatform.order.config.PayProperties;
import com.bestwo.dataplatform.order.domain.enums.OrderPayStatus;
import com.bestwo.dataplatform.order.domain.enums.OrderStatus;
import com.bestwo.dataplatform.order.domain.enums.PayNotifyProcessStatus;
import com.bestwo.dataplatform.order.domain.enums.PayNotifyType;
import com.bestwo.dataplatform.order.domain.enums.PayPlatform;
import com.bestwo.dataplatform.order.domain.enums.PaymentOrderStatus;
import com.bestwo.dataplatform.order.domain.model.BizOrder;
import com.bestwo.dataplatform.order.domain.model.BizPaymentNotifyLog;
import com.bestwo.dataplatform.order.domain.model.BizPaymentOrder;
import com.bestwo.dataplatform.order.domain.support.PaymentOrderStatusFlow;
import com.bestwo.dataplatform.order.mapper.BizOrderMapper;
import com.bestwo.dataplatform.order.mapper.BizPaymentNotifyLogMapper;
import com.bestwo.dataplatform.order.mapper.BizPaymentOrderMapper;
import com.bestwo.dataplatform.order.payment.mock.MockPaymentConstants;
import com.bestwo.dataplatform.order.payment.model.PayNotifyResult;
import com.bestwo.dataplatform.order.payment.spi.PayClientRegistry;
import com.bestwo.dataplatform.order.payment.wechat.WeChatPayNotifyVerifier;
import com.bestwo.dataplatform.order.util.OrderNoGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class PaymentNotifyService {

    private static final Logger log = LoggerFactory.getLogger(PaymentNotifyService.class);
    private static final String DEFAULT_OPERATOR = "system";
    private static final String CHANNEL_FAIL_CODE = "CHANNEL_NOTIFY_FAILED";
    private static final String CHANNEL_CLOSED_CODE = "CHANNEL_CLOSED";

    private final BizPaymentNotifyLogMapper bizPaymentNotifyLogMapper;
    private final BizPaymentOrderMapper bizPaymentOrderMapper;
    private final BizOrderMapper bizOrderMapper;
    private final PayProperties payProperties;
    private final PayClientRegistry payClientRegistry;
    private final WeChatPayNotifyVerifier weChatPayNotifyVerifier;
    private final ObjectMapper objectMapper;

    public PaymentNotifyService(
        BizPaymentNotifyLogMapper bizPaymentNotifyLogMapper,
        BizPaymentOrderMapper bizPaymentOrderMapper,
        BizOrderMapper bizOrderMapper,
        PayProperties payProperties,
        PayClientRegistry payClientRegistry,
        WeChatPayNotifyVerifier weChatPayNotifyVerifier,
        ObjectMapper objectMapper
    ) {
        this.bizPaymentNotifyLogMapper = bizPaymentNotifyLogMapper;
        this.bizPaymentOrderMapper = bizPaymentOrderMapper;
        this.bizOrderMapper = bizOrderMapper;
        this.payProperties = payProperties;
        this.payClientRegistry = payClientRegistry;
        this.weChatPayNotifyVerifier = weChatPayNotifyVerifier;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void handleWeChatPayNotify(String requestBody, HttpHeaders httpHeaders) {
        payProperties.assertWechatProvider();
        Map<String, String> headers = flattenHeaders(httpHeaders);
        JsonNode envelope = parseJson(requestBody);
        String notifyId = text(envelope, "id");
        log.info("wechat pay notify received {}", StructuredArguments.entries(buildNotifyFields(
            "wechat_pay_notify_received",
            notifyId,
            null,
            null,
            null
        )));

        BizPaymentNotifyLog notifyLog = null;
        if (StringUtils.hasText(notifyId)) {
            notifyLog = findNotifyLog(PayPlatform.WECHAT_PAY, notifyId);
            if (notifyLog != null && notifyLog.getProcessStatusEnum() != PayNotifyProcessStatus.FAILED) {
                return;
            }
        }

        if (notifyLog == null) {
            notifyLog = buildBaseNotifyLog(requestBody, headers, envelope, notifyId);
            notifyLog.setProcessStatus(PayNotifyProcessStatus.PROCESSING.getCode());

            int insertedRows = bizPaymentNotifyLogMapper.insert(notifyLog);
            if (insertedRows != 1) {
                throw new BusinessException("failed to insert payment notify log");
            }
        } else {
            resetNotifyLogForRetry(notifyLog, requestBody, headers, envelope);
            updateNotifyLog(notifyLog);
        }

        try {
            weChatPayNotifyVerifier.verifyIfConfigured(requestBody, headers);
            PayNotifyResult notifyResult = payClientRegistry.getClient(PayPlatform.WECHAT_PAY).parseNotify(requestBody, headers);
            enrichNotifyLog(notifyLog, notifyResult);

            ProcessingOutcome outcome = applyNotifyResult(notifyLog, notifyResult);
            notifyLog.setProcessStatus(outcome.status().getCode());
            notifyLog.setProcessMessage(truncate(outcome.message(), 255));
            notifyLog.setProcessedAt(Instant.now());

            updateNotifyLog(notifyLog);
            log.info("wechat pay notify processed {}", StructuredArguments.entries(buildNotifyFields(
                "wechat_pay_notify_processed",
                notifyLog.getNotifyId(),
                notifyLog.getOrderNo(),
                notifyLog.getPaymentOrderNo(),
                notifyLog.getProcessStatus()
            )));
        } catch (RuntimeException exception) {
            notifyLog.setProcessStatus(PayNotifyProcessStatus.FAILED.getCode());
            notifyLog.setProcessMessage(truncate(exception.getMessage(), 255));
            notifyLog.setProcessedAt(Instant.now());
            updateNotifyLog(notifyLog);
            log.error("wechat pay notify failed {}", StructuredArguments.entries(buildNotifyFields(
                "wechat_pay_notify_failed",
                notifyLog.getNotifyId(),
                notifyLog.getOrderNo(),
                notifyLog.getPaymentOrderNo(),
                notifyLog.getProcessStatus()
            )), exception);
            throw exception;
        }
    }

    @Transactional
    public void handleMockPayNotify(String requestBody) {
        payProperties.assertMockProvider();
        Map<String, String> headers = Map.of("X-Mock-Provider", MockPaymentConstants.PROVIDER);
        JsonNode envelope = parseJson(requestBody);
        String notifyId = firstNonBlank(text(envelope, "notifyNo"), text(envelope, "notifyId"), text(envelope, "notify_id"));
        log.info("mock pay notify received {}", StructuredArguments.entries(buildNotifyFields(
            "mock_pay_notify_received",
            notifyId,
            null,
            null,
            null
        )));

        BizPaymentNotifyLog notifyLog = null;
        if (StringUtils.hasText(notifyId)) {
            notifyLog = findNotifyLog(PayPlatform.WECHAT_PAY, notifyId);
            if (notifyLog != null && notifyLog.getProcessStatusEnum() != PayNotifyProcessStatus.FAILED) {
                return;
            }
        }

        if (notifyLog == null) {
            notifyLog = buildMockNotifyLog(requestBody, headers, envelope, notifyId);
            notifyLog.setProcessStatus(PayNotifyProcessStatus.PROCESSING.getCode());

            int insertedRows = bizPaymentNotifyLogMapper.insert(notifyLog);
            if (insertedRows != 1) {
                throw new BusinessException("failed to insert payment notify log");
            }
        } else {
            resetMockNotifyLogForRetry(notifyLog, requestBody, headers, envelope);
            updateNotifyLog(notifyLog);
        }

        try {
            PayNotifyResult notifyResult = payClientRegistry.getClient(PayPlatform.WECHAT_PAY).parseNotify(requestBody, headers);
            BizPaymentOrder paymentOrder = findPaymentOrder(notifyResult.paymentOrderNo(), notifyResult.merchantOrderNo());
            if (paymentOrder != null && !isMockPaymentOrder(paymentOrder)) {
                throw new BusinessException("current payment order is not in mock mode");
            }
            enrichNotifyLog(notifyLog, notifyResult);

            ProcessingOutcome outcome = applyNotifyResult(notifyLog, notifyResult);
            notifyLog.setProcessStatus(outcome.status().getCode());
            notifyLog.setProcessMessage(truncate(outcome.message(), 255));
            notifyLog.setProcessedAt(Instant.now());

            updateNotifyLog(notifyLog);
            log.info("mock pay notify processed {}", StructuredArguments.entries(buildNotifyFields(
                "mock_pay_notify_processed",
                notifyLog.getNotifyId(),
                notifyLog.getOrderNo(),
                notifyLog.getPaymentOrderNo(),
                notifyLog.getProcessStatus()
            )));
        } catch (RuntimeException exception) {
            notifyLog.setProcessStatus(PayNotifyProcessStatus.FAILED.getCode());
            notifyLog.setProcessMessage(truncate(exception.getMessage(), 255));
            notifyLog.setProcessedAt(Instant.now());
            updateNotifyLog(notifyLog);
            log.error("mock pay notify failed {}", StructuredArguments.entries(buildNotifyFields(
                "mock_pay_notify_failed",
                notifyLog.getNotifyId(),
                notifyLog.getOrderNo(),
                notifyLog.getPaymentOrderNo(),
                notifyLog.getProcessStatus()
            )), exception);
            throw exception;
        }
    }

    private ProcessingOutcome applyNotifyResult(BizPaymentNotifyLog notifyLog, PayNotifyResult notifyResult) {
        BizPaymentOrder paymentOrder = findPaymentOrder(notifyLog.getPaymentOrderNo(), notifyLog.getOrderNo());
        if (paymentOrder == null) {
            throw new BusinessException("payment order not found");
        }

        if (!StringUtils.hasText(notifyLog.getPaymentOrderId())) {
            notifyLog.setPaymentOrderId(paymentOrder.getPaymentOrderId());
        }
        if (!StringUtils.hasText(notifyLog.getOrderId())) {
            notifyLog.setOrderId(paymentOrder.getOrderId());
        }
        if (!StringUtils.hasText(notifyLog.getOrderNo())) {
            notifyLog.setOrderNo(paymentOrder.getOrderNo());
        }
        if (!StringUtils.hasText(notifyLog.getPaymentOrderNo())) {
            notifyLog.setPaymentOrderNo(paymentOrder.getPaymentOrderNo());
        }

        BizOrder order = findOrder(paymentOrder, notifyLog.getOrderNo());
        if (order == null) {
            throw new BusinessException("order not found for payment notify");
        }

        StatusApplyResult paymentResult = applyPaymentOrderUpdate(paymentOrder, notifyResult);
        StatusApplyResult orderResult = applyOrderUpdate(order, paymentOrder, notifyResult);

        PayNotifyProcessStatus processStatus =
            paymentResult.changed() || orderResult.changed() ? PayNotifyProcessStatus.PROCESSED : PayNotifyProcessStatus.IGNORED;
        String processMessage = "payment=" + paymentResult.message() + "; order=" + orderResult.message();
        return new ProcessingOutcome(processStatus, processMessage);
    }

    private StatusApplyResult applyPaymentOrderUpdate(BizPaymentOrder paymentOrder, PayNotifyResult notifyResult) {
        PaymentOrderStatus currentStatus = paymentOrder.getStatusEnum();
        PaymentOrderStatus incomingStatus = notifyResult.status();
        Instant now = Instant.now();

        paymentOrder.setLastNotifyTime(now);
        paymentOrder.setUpdatedBy(DEFAULT_OPERATOR);
        paymentOrder.setUpdatedAt(now);
        if (StringUtils.hasText(notifyResult.channelOrderNo())) {
            paymentOrder.setChannelOrderNo(notifyResult.channelOrderNo());
        }
        if (StringUtils.hasText(notifyResult.rawBody())) {
            paymentOrder.setExtJson(notifyResult.rawBody());
        }

        if (incomingStatus == null) {
            updatePaymentOrder(paymentOrder);
            return new StatusApplyResult(true, "notify received without channel status");
        }

        if (currentStatus == incomingStatus) {
            if (incomingStatus == PaymentOrderStatus.SUCCESS && notifyResult.amountFen() != null) {
                paymentOrder.setSuccessAmountFen(notifyResult.amountFen());
            }
            if (incomingStatus == PaymentOrderStatus.SUCCESS && notifyResult.paidAt() != null) {
                paymentOrder.setSuccessTime(notifyResult.paidAt());
            }
            updatePaymentOrder(paymentOrder);
            return new StatusApplyResult(false, "duplicate payment status " + incomingStatus.getCode());
        }

        if (currentStatus.isTerminal()) {
            updatePaymentOrder(paymentOrder);
            return new StatusApplyResult(
                false,
                "ignored notify because payment order is already " + currentStatus.getCode()
            );
        }

        if (!PaymentOrderStatusFlow.canTransition(currentStatus, incomingStatus)) {
            throw new BusinessException(
                "illegal payment order status transition: " + currentStatus.getCode() + " -> " + incomingStatus.getCode()
            );
        }

        paymentOrder.setStatus(incomingStatus.getCode());
        switch (incomingStatus) {
            case SUCCESS -> {
                paymentOrder.setSuccessAmountFen(notifyResult.amountFen() == null
                    ? paymentOrder.getRequestAmountFen()
                    : notifyResult.amountFen());
                paymentOrder.setSuccessTime(notifyResult.paidAt() == null ? now : notifyResult.paidAt());
                paymentOrder.setFailCode(null);
                paymentOrder.setFailMessage(null);
            }
            case CLOSED -> {
                paymentOrder.setClosedTime(now);
                paymentOrder.setFailCode(CHANNEL_CLOSED_CODE);
                paymentOrder.setFailMessage(truncate(resolveNotifyFailMessage(notifyResult, "payment closed by channel notify"), 255));
            }
            case FAILED -> {
                paymentOrder.setFailCode(CHANNEL_FAIL_CODE);
                paymentOrder.setFailMessage(truncate(resolveNotifyFailMessage(notifyResult, "payment failed by channel notify"), 255));
            }
            case WAIT_PAY -> {
                if (notifyResult.paidAt() != null) {
                    paymentOrder.setExpiredTime(notifyResult.paidAt());
                }
            }
            default -> {
            }
        }

        updatePaymentOrder(paymentOrder);
        return new StatusApplyResult(true, "payment order moved to " + incomingStatus.getCode());
    }

    private StatusApplyResult applyOrderUpdate(BizOrder order, BizPaymentOrder paymentOrder, PayNotifyResult notifyResult) {
        PaymentOrderStatus incomingStatus = notifyResult.status();
        if (incomingStatus == null) {
            return new StatusApplyResult(false, "order skipped because payment status is empty");
        }

        Instant now = Instant.now();
        boolean changed = false;

        order.setLatestPaymentOrderNo(paymentOrder.getPaymentOrderNo());
        if (StringUtils.hasText(paymentOrder.getChannelOrderNo())) {
            order.setLatestChannelOrderNo(paymentOrder.getChannelOrderNo());
        }
        order.setUpdatedBy(DEFAULT_OPERATOR);
        order.setUpdatedAt(now);

        OrderStatus currentOrderStatus = order.getOrderStatusEnum();
        OrderPayStatus currentPayStatus = order.getPayStatusEnum();

        switch (incomingStatus) {
            case SUCCESS -> {
                if (currentOrderStatus == OrderStatus.PAID && currentPayStatus == OrderPayStatus.PAID) {
                    if (notifyResult.amountFen() != null) {
                        order.setPaidAmountFen(notifyResult.amountFen());
                    }
                    if (notifyResult.paidAt() != null) {
                        order.setPaidTime(notifyResult.paidAt());
                    }
                    updateOrder(order);
                    return new StatusApplyResult(false, "duplicate paid notify");
                }

                if (currentOrderStatus == OrderStatus.CLOSED
                    || currentOrderStatus == OrderStatus.CANCELLED
                    || currentOrderStatus == OrderStatus.REFUNDING
                    || currentOrderStatus == OrderStatus.REFUNDED) {
                    updateOrder(order);
                    return new StatusApplyResult(false, "ignored paid notify because order is " + currentOrderStatus.getCode());
                }

                order.setOrderStatus(OrderStatus.PAID.getCode());
                order.setPayStatus(OrderPayStatus.PAID.getCode());
                order.setSuccessPayPlatform(paymentOrder.getPlatform());
                order.setPaidAmountFen(notifyResult.amountFen() == null
                    ? paymentOrder.getSuccessAmountFen()
                    : notifyResult.amountFen());
                order.setPaidTime(notifyResult.paidAt() == null ? now : notifyResult.paidAt());
                changed = true;
            }
            case CLOSED -> {
                if (currentOrderStatus == OrderStatus.PAID
                    || currentOrderStatus == OrderStatus.REFUNDING
                    || currentOrderStatus == OrderStatus.REFUNDED) {
                    updateOrder(order);
                    return new StatusApplyResult(false, "ignored closed notify because order is " + currentOrderStatus.getCode());
                }
                if (currentOrderStatus == OrderStatus.CLOSED && currentPayStatus == OrderPayStatus.CLOSED) {
                    updateOrder(order);
                    return new StatusApplyResult(false, "duplicate closed notify");
                }
                order.setOrderStatus(OrderStatus.CLOSED.getCode());
                order.setPayStatus(OrderPayStatus.CLOSED.getCode());
                order.setClosedTime(now);
                changed = true;
            }
            case FAILED -> {
                if (currentOrderStatus == OrderStatus.PAID
                    || currentOrderStatus == OrderStatus.REFUNDING
                    || currentOrderStatus == OrderStatus.REFUNDED) {
                    updateOrder(order);
                    return new StatusApplyResult(false, "ignored failed notify because order is " + currentOrderStatus.getCode());
                }
                if (currentOrderStatus == OrderStatus.CREATED) {
                    order.setOrderStatus(OrderStatus.WAIT_PAY.getCode());
                    changed = true;
                } else if (currentOrderStatus == OrderStatus.PAYING) {
                    order.setOrderStatus(OrderStatus.WAIT_PAY.getCode());
                    changed = true;
                }
                if (currentPayStatus != OrderPayStatus.UNPAID) {
                    order.setPayStatus(OrderPayStatus.UNPAID.getCode());
                    changed = true;
                }
            }
            case WAIT_PAY -> {
                if (currentOrderStatus == OrderStatus.CREATED) {
                    order.setOrderStatus(OrderStatus.WAIT_PAY.getCode());
                    changed = true;
                }
                if (currentPayStatus == OrderPayStatus.PAYING) {
                    order.setPayStatus(OrderPayStatus.UNPAID.getCode());
                    changed = true;
                }
                order.setExpiredTime(paymentOrder.getExpiredTime());
            }
            default -> {
                return new StatusApplyResult(false, "order skipped for payment status " + incomingStatus.getCode());
            }
        }

        updateOrder(order);
        return new StatusApplyResult(changed, changed ? "order updated to " + order.getOrderStatus() : "order unchanged");
    }

    private BizOrder findOrder(BizPaymentOrder paymentOrder, String orderNo) {
        if (StringUtils.hasText(paymentOrder.getOrderId())) {
            BizOrder order = bizOrderMapper.selectOne(
                new LambdaQueryWrapper<BizOrder>()
                    .eq(BizOrder::getOrderId, paymentOrder.getOrderId())
                    .last("LIMIT 1")
            );
            if (order != null) {
                return order;
            }
        }
        if (StringUtils.hasText(orderNo)) {
            return bizOrderMapper.selectOne(
                new LambdaQueryWrapper<BizOrder>()
                    .eq(BizOrder::getOrderNo, orderNo)
                    .last("LIMIT 1")
            );
        }
        return null;
    }

    private BizPaymentNotifyLog findNotifyLog(PayPlatform platform, String notifyId) {
        return bizPaymentNotifyLogMapper.selectOne(
            new LambdaQueryWrapper<BizPaymentNotifyLog>()
                .eq(BizPaymentNotifyLog::getPlatform, platform.getCode())
                .eq(BizPaymentNotifyLog::getNotifyId, notifyId)
                .last("LIMIT 1")
        );
    }

    private BizPaymentNotifyLog buildBaseNotifyLog(
        String requestBody,
        Map<String, String> headers,
        JsonNode envelope,
        String notifyId
    ) {
        BizPaymentNotifyLog notifyLog = new BizPaymentNotifyLog();
        notifyLog.setNotifyLogId(OrderNoGenerator.generateNotifyLogId());
        notifyLog.setNotifyId(trimToNull(notifyId));
        notifyLog.setPlatform(PayPlatform.WECHAT_PAY.getCode());
        notifyLog.setNotifyType(PayNotifyType.PAY.getCode());
        notifyLog.setEventType(trimToNull(text(envelope, "event_type")));
        notifyLog.setSummary(trimToNull(text(envelope, "summary")));
        notifyLog.setRequestHeadersJson(toJson(headers));
        notifyLog.setRequestBody(requestBody);
        notifyLog.setEncryptType(trimToNull(text(envelope.path("resource"), "algorithm")));
        notifyLog.setResourceCiphertext(trimToNull(text(envelope.path("resource"), "ciphertext")));
        notifyLog.setResourceNonce(trimToNull(text(envelope.path("resource"), "nonce")));
        notifyLog.setResourceAssociatedData(trimToNull(text(envelope.path("resource"), "associated_data")));
        notifyLog.setSignatureSerialNo(trimToNull(header(headers, "Wechatpay-Serial")));
        notifyLog.setSignatureValue(trimToNull(header(headers, "Wechatpay-Signature")));
        notifyLog.setReceivedAt(Instant.now());
        return notifyLog;
    }

    private BizPaymentNotifyLog buildMockNotifyLog(
        String requestBody,
        Map<String, String> headers,
        JsonNode envelope,
        String notifyId
    ) {
        BizPaymentNotifyLog notifyLog = new BizPaymentNotifyLog();
        notifyLog.setNotifyLogId(OrderNoGenerator.generateNotifyLogId());
        notifyLog.setNotifyId(trimToNull(notifyId));
        notifyLog.setPlatform(PayPlatform.WECHAT_PAY.getCode());
        notifyLog.setNotifyType(PayNotifyType.PAY.getCode());
        notifyLog.setEventType(MockPaymentConstants.EVENT_TYPE);
        notifyLog.setEventStatus(trimToNull(text(envelope, "tradeState")));
        notifyLog.setSummary("mock payment notify");
        notifyLog.setRequestHeadersJson(toJson(headers));
        notifyLog.setRequestBody(requestBody);
        notifyLog.setReceivedAt(Instant.now());
        return notifyLog;
    }

    private void resetNotifyLogForRetry(
        BizPaymentNotifyLog notifyLog,
        String requestBody,
        Map<String, String> headers,
        JsonNode envelope
    ) {
        notifyLog.setEventType(trimToNull(text(envelope, "event_type")));
        notifyLog.setSummary(trimToNull(text(envelope, "summary")));
        notifyLog.setRequestHeadersJson(toJson(headers));
        notifyLog.setRequestBody(requestBody);
        notifyLog.setEncryptType(trimToNull(text(envelope.path("resource"), "algorithm")));
        notifyLog.setResourceCiphertext(trimToNull(text(envelope.path("resource"), "ciphertext")));
        notifyLog.setResourceNonce(trimToNull(text(envelope.path("resource"), "nonce")));
        notifyLog.setResourceAssociatedData(trimToNull(text(envelope.path("resource"), "associated_data")));
        notifyLog.setSignatureSerialNo(trimToNull(header(headers, "Wechatpay-Serial")));
        notifyLog.setSignatureValue(trimToNull(header(headers, "Wechatpay-Signature")));
        notifyLog.setProcessStatus(PayNotifyProcessStatus.PROCESSING.getCode());
        notifyLog.setProcessMessage("retrying failed callback");
        notifyLog.setProcessedAt(null);
    }

    private void resetMockNotifyLogForRetry(
        BizPaymentNotifyLog notifyLog,
        String requestBody,
        Map<String, String> headers,
        JsonNode envelope
    ) {
        notifyLog.setEventType(MockPaymentConstants.EVENT_TYPE);
        notifyLog.setEventStatus(trimToNull(text(envelope, "tradeState")));
        notifyLog.setSummary("retrying mock payment notify");
        notifyLog.setRequestHeadersJson(toJson(headers));
        notifyLog.setRequestBody(requestBody);
        notifyLog.setEncryptType(null);
        notifyLog.setResourceCiphertext(null);
        notifyLog.setResourceNonce(null);
        notifyLog.setResourceAssociatedData(null);
        notifyLog.setSignatureSerialNo(null);
        notifyLog.setSignatureValue(null);
        notifyLog.setProcessStatus(PayNotifyProcessStatus.PROCESSING.getCode());
        notifyLog.setProcessMessage("retrying failed callback");
        notifyLog.setProcessedAt(null);
    }

    private void enrichNotifyLog(BizPaymentNotifyLog notifyLog, PayNotifyResult notifyResult) {
        notifyLog.setNotifyId(
            trimToNull(notifyResult.notifyId()) == null ? notifyLog.getNotifyId() : trimToNull(notifyResult.notifyId())
        );
        notifyLog.setPaymentOrderNo(trimToNull(notifyResult.paymentOrderNo()));
        notifyLog.setOrderNo(trimToNull(notifyResult.merchantOrderNo()));
        notifyLog.setChannelOrderNo(trimToNull(notifyResult.channelOrderNo()));
        notifyLog.setEventStatus(notifyResult.status() == null ? null : notifyResult.status().getCode());
        notifyLog.setExtJson(trimToNull(notifyResult.rawBody()));

        BizPaymentOrder paymentOrder = findPaymentOrder(notifyResult.paymentOrderNo(), notifyResult.merchantOrderNo());
        if (paymentOrder != null) {
            notifyLog.setPaymentOrderId(paymentOrder.getPaymentOrderId());
            notifyLog.setPaymentOrderNo(paymentOrder.getPaymentOrderNo());
            notifyLog.setOrderId(paymentOrder.getOrderId());
            notifyLog.setOrderNo(paymentOrder.getOrderNo());
        }

        if (!StringUtils.hasText(notifyLog.getOrderId()) && StringUtils.hasText(notifyResult.merchantOrderNo())) {
            BizOrder order = bizOrderMapper.selectOne(
                new LambdaQueryWrapper<BizOrder>()
                    .eq(BizOrder::getOrderNo, notifyResult.merchantOrderNo())
                    .last("LIMIT 1")
            );
            if (order != null) {
                notifyLog.setOrderId(order.getOrderId());
                notifyLog.setOrderNo(order.getOrderNo());
            }
        }
    }

    private BizPaymentOrder findPaymentOrder(String paymentOrderNo, String orderNo) {
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

        if (StringUtils.hasText(orderNo)) {
            return bizPaymentOrderMapper.selectOne(
                new LambdaQueryWrapper<BizPaymentOrder>()
                    .eq(BizPaymentOrder::getOrderNo, orderNo)
                    .orderByDesc(BizPaymentOrder::getCreatedAt)
                    .last("LIMIT 1")
            );
        }
        return null;
    }

    private void updateNotifyLog(BizPaymentNotifyLog notifyLog) {
        if (notifyLog.getId() == null) {
            return;
        }
        int affectedRows = bizPaymentNotifyLogMapper.updateById(notifyLog);
        if (affectedRows != 1) {
            throw new BusinessException("failed to update payment notify log");
        }
    }

    private void updatePaymentOrder(BizPaymentOrder paymentOrder) {
        int affectedRows = bizPaymentOrderMapper.updateById(paymentOrder);
        if (affectedRows != 1) {
            throw new BusinessException("failed to update payment order");
        }
    }

    private void updateOrder(BizOrder order) {
        int affectedRows = bizOrderMapper.updateById(order);
        if (affectedRows != 1) {
            throw new BusinessException("failed to update order");
        }
    }

    private JsonNode parseJson(String requestBody) {
        try {
            return objectMapper.readTree(requestBody);
        } catch (JsonProcessingException exception) {
            throw new BusinessException("invalid pay notify body");
        }
    }

    private Map<String, String> flattenHeaders(HttpHeaders httpHeaders) {
        Map<String, String> headers = new LinkedHashMap<>();
        httpHeaders.forEach((key, values) -> headers.put(key, values == null || values.isEmpty() ? null : values.get(0)));
        return headers;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException("failed to serialize payment notify headers");
        }
    }

    private String header(Map<String, String> headers, String key) {
        return headers.entrySet().stream()
            .filter(entry -> entry.getKey() != null && entry.getKey().equalsIgnoreCase(key))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null);
    }

    private String text(JsonNode node, String fieldName) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        JsonNode field = node.path(fieldName);
        return field.isMissingNode() || field.isNull() ? null : field.asText();
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

    private String resolveNotifyFailMessage(PayNotifyResult notifyResult, String fallbackMessage) {
        return StringUtils.hasText(notifyResult.failMessage()) ? notifyResult.failMessage().trim() : fallbackMessage;
    }

    private boolean isMockPaymentOrder(BizPaymentOrder paymentOrder) {
        return MockPaymentConstants.MERCHANT_CODE.equalsIgnoreCase(paymentOrder.getMerchantCode())
            || (paymentOrder.getChannelPrepayId() != null && paymentOrder.getChannelPrepayId().startsWith("mock_"));
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private Map<String, Object> buildNotifyFields(
        String event,
        String notifyId,
        String orderNo,
        String paymentOrderNo,
        String processStatus
    ) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("event", event);
        if (StringUtils.hasText(notifyId)) {
            fields.put("notifyId", notifyId);
        }
        if (StringUtils.hasText(orderNo)) {
            fields.put("orderNo", orderNo);
        }
        if (StringUtils.hasText(paymentOrderNo)) {
            fields.put("paymentOrderNo", paymentOrderNo);
        }
        if (StringUtils.hasText(processStatus)) {
            fields.put("processStatus", processStatus);
        }
        return fields;
    }

    private record ProcessingOutcome(PayNotifyProcessStatus status, String message) {}

    private record StatusApplyResult(boolean changed, String message) {}
}
