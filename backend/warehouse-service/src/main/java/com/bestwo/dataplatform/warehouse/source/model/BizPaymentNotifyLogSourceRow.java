package com.bestwo.dataplatform.warehouse.source.model;

import java.sql.Timestamp;

public class BizPaymentNotifyLogSourceRow {

    private Long id;
    private String notifyLogId;
    private String notifyId;
    private String platform;
    private String notifyType;
    private String paymentOrderId;
    private String paymentOrderNo;
    private String orderId;
    private String orderNo;
    private String channelOrderNo;
    private String eventType;
    private String eventStatus;
    private String summary;
    private String requestHeadersJson;
    private String requestBody;
    private String encryptType;
    private String resourceCiphertext;
    private String resourceNonce;
    private String resourceAssociatedData;
    private String signatureSerialNo;
    private String signatureValue;
    private String processStatus;
    private String processMessage;
    private Timestamp processedAt;
    private Timestamp receivedAt;
    private String extJson;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNotifyLogId() {
        return notifyLogId;
    }

    public void setNotifyLogId(String notifyLogId) {
        this.notifyLogId = notifyLogId;
    }

    public String getNotifyId() {
        return notifyId;
    }

    public void setNotifyId(String notifyId) {
        this.notifyId = notifyId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getNotifyType() {
        return notifyType;
    }

    public void setNotifyType(String notifyType) {
        this.notifyType = notifyType;
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

    public String getChannelOrderNo() {
        return channelOrderNo;
    }

    public void setChannelOrderNo(String channelOrderNo) {
        this.channelOrderNo = channelOrderNo;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(String eventStatus) {
        this.eventStatus = eventStatus;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getRequestHeadersJson() {
        return requestHeadersJson;
    }

    public void setRequestHeadersJson(String requestHeadersJson) {
        this.requestHeadersJson = requestHeadersJson;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getEncryptType() {
        return encryptType;
    }

    public void setEncryptType(String encryptType) {
        this.encryptType = encryptType;
    }

    public String getResourceCiphertext() {
        return resourceCiphertext;
    }

    public void setResourceCiphertext(String resourceCiphertext) {
        this.resourceCiphertext = resourceCiphertext;
    }

    public String getResourceNonce() {
        return resourceNonce;
    }

    public void setResourceNonce(String resourceNonce) {
        this.resourceNonce = resourceNonce;
    }

    public String getResourceAssociatedData() {
        return resourceAssociatedData;
    }

    public void setResourceAssociatedData(String resourceAssociatedData) {
        this.resourceAssociatedData = resourceAssociatedData;
    }

    public String getSignatureSerialNo() {
        return signatureSerialNo;
    }

    public void setSignatureSerialNo(String signatureSerialNo) {
        this.signatureSerialNo = signatureSerialNo;
    }

    public String getSignatureValue() {
        return signatureValue;
    }

    public void setSignatureValue(String signatureValue) {
        this.signatureValue = signatureValue;
    }

    public String getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(String processStatus) {
        this.processStatus = processStatus;
    }

    public String getProcessMessage() {
        return processMessage;
    }

    public void setProcessMessage(String processMessage) {
        this.processMessage = processMessage;
    }

    public Timestamp getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Timestamp processedAt) {
        this.processedAt = processedAt;
    }

    public Timestamp getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Timestamp receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getExtJson() {
        return extJson;
    }

    public void setExtJson(String extJson) {
        this.extJson = extJson;
    }
}
