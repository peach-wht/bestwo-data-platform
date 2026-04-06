package com.bestwo.dataplatform.order.dto;

import java.time.Instant;

public class OrderListItemResponse {

    private String orderId;
    private String orderNo;
    private String orderTitle;
    private String buyerId;
    private String buyerNickname;
    private Long totalAmountFen;
    private Long payableAmountFen;
    private Long paidAmountFen;
    private String orderStatus;
    private String payStatus;
    private String preferredPayPlatform;
    private String preferredTradeType;
    private String latestPaymentOrderNo;
    private Instant paidTime;
    private Instant createdAt;

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

    public String getOrderTitle() {
        return orderTitle;
    }

    public void setOrderTitle(String orderTitle) {
        this.orderTitle = orderTitle;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerNickname() {
        return buyerNickname;
    }

    public void setBuyerNickname(String buyerNickname) {
        this.buyerNickname = buyerNickname;
    }

    public Long getTotalAmountFen() {
        return totalAmountFen;
    }

    public void setTotalAmountFen(Long totalAmountFen) {
        this.totalAmountFen = totalAmountFen;
    }

    public Long getPayableAmountFen() {
        return payableAmountFen;
    }

    public void setPayableAmountFen(Long payableAmountFen) {
        this.payableAmountFen = payableAmountFen;
    }

    public Long getPaidAmountFen() {
        return paidAmountFen;
    }

    public void setPaidAmountFen(Long paidAmountFen) {
        this.paidAmountFen = paidAmountFen;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(String payStatus) {
        this.payStatus = payStatus;
    }

    public String getPreferredPayPlatform() {
        return preferredPayPlatform;
    }

    public void setPreferredPayPlatform(String preferredPayPlatform) {
        this.preferredPayPlatform = preferredPayPlatform;
    }

    public String getPreferredTradeType() {
        return preferredTradeType;
    }

    public void setPreferredTradeType(String preferredTradeType) {
        this.preferredTradeType = preferredTradeType;
    }

    public String getLatestPaymentOrderNo() {
        return latestPaymentOrderNo;
    }

    public void setLatestPaymentOrderNo(String latestPaymentOrderNo) {
        this.latestPaymentOrderNo = latestPaymentOrderNo;
    }

    public Instant getPaidTime() {
        return paidTime;
    }

    public void setPaidTime(Instant paidTime) {
        this.paidTime = paidTime;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
