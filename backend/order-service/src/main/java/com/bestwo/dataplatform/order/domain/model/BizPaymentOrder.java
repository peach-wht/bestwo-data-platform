package com.bestwo.dataplatform.order.domain.model;

import com.bestwo.dataplatform.order.domain.enums.PayPlatform;
import com.bestwo.dataplatform.order.domain.enums.PayTradeType;
import com.bestwo.dataplatform.order.domain.enums.PaymentOrderStatus;
import java.time.Instant;

public class BizPaymentOrder {

    private Long id;
    private String paymentOrderId;
    private String paymentOrderNo;
    private String orderId;
    private String orderNo;
    private String platform;
    private String tradeType;
    private String merchantCode;
    private String channelAppId;
    private String channelMerchantId;
    private String subject;
    private String description;
    private String currency;
    private Long requestAmountFen;
    private Long successAmountFen;
    private String payerId;
    private String clientIp;
    private String notifyUrl;
    private String channelOrderNo;
    private String channelPrepayId;
    private String codeUrl;
    private String status;
    private String failCode;
    private String failMessage;
    private Instant expiredTime;
    private Instant successTime;
    private Instant closedTime;
    private Instant lastNotifyTime;
    private String extJson;
    private Integer version;
    private String createdBy;
    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;

    public PayPlatform getPlatformEnum() {
        return PayPlatform.fromCode(platform);
    }

    public PayTradeType getTradeTypeEnum() {
        return PayTradeType.fromCode(tradeType);
    }

    public PaymentOrderStatus getStatusEnum() {
        return PaymentOrderStatus.fromCode(status);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPaymentOrderId() {
        return paymentOrderId;
    }

    public void setPaymentOrderId(String paymentOrderId) {
        this.paymentOrderId = paymentOrderId;
    }

    public String getPaymentOrderNo() {
        return paymentOrderNo;
    }

    public void setPaymentOrderNo(String paymentOrderNo) {
        this.paymentOrderNo = paymentOrderNo;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }

    public String getChannelAppId() {
        return channelAppId;
    }

    public void setChannelAppId(String channelAppId) {
        this.channelAppId = channelAppId;
    }

    public String getChannelMerchantId() {
        return channelMerchantId;
    }

    public void setChannelMerchantId(String channelMerchantId) {
        this.channelMerchantId = channelMerchantId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Long getRequestAmountFen() {
        return requestAmountFen;
    }

    public void setRequestAmountFen(Long requestAmountFen) {
        this.requestAmountFen = requestAmountFen;
    }

    public Long getSuccessAmountFen() {
        return successAmountFen;
    }

    public void setSuccessAmountFen(Long successAmountFen) {
        this.successAmountFen = successAmountFen;
    }

    public String getPayerId() {
        return payerId;
    }

    public void setPayerId(String payerId) {
        this.payerId = payerId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getChannelOrderNo() {
        return channelOrderNo;
    }

    public void setChannelOrderNo(String channelOrderNo) {
        this.channelOrderNo = channelOrderNo;
    }

    public String getChannelPrepayId() {
        return channelPrepayId;
    }

    public void setChannelPrepayId(String channelPrepayId) {
        this.channelPrepayId = channelPrepayId;
    }

    public String getCodeUrl() {
        return codeUrl;
    }

    public void setCodeUrl(String codeUrl) {
        this.codeUrl = codeUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFailCode() {
        return failCode;
    }

    public void setFailCode(String failCode) {
        this.failCode = failCode;
    }

    public String getFailMessage() {
        return failMessage;
    }

    public void setFailMessage(String failMessage) {
        this.failMessage = failMessage;
    }

    public Instant getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(Instant expiredTime) {
        this.expiredTime = expiredTime;
    }

    public Instant getSuccessTime() {
        return successTime;
    }

    public void setSuccessTime(Instant successTime) {
        this.successTime = successTime;
    }

    public Instant getClosedTime() {
        return closedTime;
    }

    public void setClosedTime(Instant closedTime) {
        this.closedTime = closedTime;
    }

    public Instant getLastNotifyTime() {
        return lastNotifyTime;
    }

    public void setLastNotifyTime(Instant lastNotifyTime) {
        this.lastNotifyTime = lastNotifyTime;
    }

    public String getExtJson() {
        return extJson;
    }

    public void setExtJson(String extJson) {
        this.extJson = extJson;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
