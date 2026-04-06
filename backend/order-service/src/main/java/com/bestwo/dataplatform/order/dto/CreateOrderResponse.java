package com.bestwo.dataplatform.order.dto;

import java.time.Instant;

public class CreateOrderResponse {

    private String orderId;
    private String orderNo;
    private String orderStatus;
    private String payStatus;
    private Long totalAmountFen;
    private Long payableAmountFen;
    private String preferredPayPlatform;
    private String preferredTradeType;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
