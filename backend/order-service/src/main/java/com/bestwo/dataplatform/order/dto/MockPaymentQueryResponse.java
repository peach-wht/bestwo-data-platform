package com.bestwo.dataplatform.order.dto;

import java.time.Instant;

public class MockPaymentQueryResponse {

    private String paymentOrderNo;
    private String orderNo;
    private String status;
    private String platform;
    private String tradeType;
    private String paymentProvider;
    private Boolean mockMode;
    private String mockPayToken;
    private String mockPayUrl;
    private String channelOrderNo;
    private Instant successTime;
    private String failMessage;

    public String getPaymentOrderNo() {
        return paymentOrderNo;
    }

    public void setPaymentOrderNo(String paymentOrderNo) {
        this.paymentOrderNo = paymentOrderNo;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getPaymentProvider() {
        return paymentProvider;
    }

    public void setPaymentProvider(String paymentProvider) {
        this.paymentProvider = paymentProvider;
    }

    public Boolean getMockMode() {
        return mockMode;
    }

    public void setMockMode(Boolean mockMode) {
        this.mockMode = mockMode;
    }

    public String getMockPayToken() {
        return mockPayToken;
    }

    public void setMockPayToken(String mockPayToken) {
        this.mockPayToken = mockPayToken;
    }

    public String getMockPayUrl() {
        return mockPayUrl;
    }

    public void setMockPayUrl(String mockPayUrl) {
        this.mockPayUrl = mockPayUrl;
    }

    public String getChannelOrderNo() {
        return channelOrderNo;
    }

    public void setChannelOrderNo(String channelOrderNo) {
        this.channelOrderNo = channelOrderNo;
    }

    public Instant getSuccessTime() {
        return successTime;
    }

    public void setSuccessTime(Instant successTime) {
        this.successTime = successTime;
    }

    public String getFailMessage() {
        return failMessage;
    }

    public void setFailMessage(String failMessage) {
        this.failMessage = failMessage;
    }
}
