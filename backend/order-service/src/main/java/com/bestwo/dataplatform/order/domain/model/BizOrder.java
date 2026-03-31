package com.bestwo.dataplatform.order.domain.model;

import com.bestwo.dataplatform.order.domain.enums.OrderPayStatus;
import com.bestwo.dataplatform.order.domain.enums.OrderStatus;
import java.time.Instant;

public class BizOrder {

    private Long id;
    private String orderId;
    private String orderNo;
    private String externalOrderNo;
    private String bizType;
    private String orderSource;
    private String orderTitle;
    private String orderDescription;
    private String buyerId;
    private String buyerNickname;
    private String currency;
    private Long totalAmountFen;
    private Long payableAmountFen;
    private Long paidAmountFen;
    private Long refundedAmountFen;
    private String orderStatus;
    private String payStatus;
    private String preferredPayPlatform;
    private String preferredTradeType;
    private String successPayPlatform;
    private String latestPaymentOrderNo;
    private String latestChannelOrderNo;
    private Instant paidTime;
    private Instant expiredTime;
    private Instant closedTime;
    private Instant cancelledTime;
    private String remark;
    private String extJson;
    private Integer version;
    private String createdBy;
    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;

    public OrderStatus getOrderStatusEnum() {
        return OrderStatus.fromCode(orderStatus);
    }

    public OrderPayStatus getPayStatusEnum() {
        return OrderPayStatus.fromCode(payStatus);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getExternalOrderNo() {
        return externalOrderNo;
    }

    public void setExternalOrderNo(String externalOrderNo) {
        this.externalOrderNo = externalOrderNo;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getOrderSource() {
        return orderSource;
    }

    public void setOrderSource(String orderSource) {
        this.orderSource = orderSource;
    }

    public String getOrderTitle() {
        return orderTitle;
    }

    public void setOrderTitle(String orderTitle) {
        this.orderTitle = orderTitle;
    }

    public String getOrderDescription() {
        return orderDescription;
    }

    public void setOrderDescription(String orderDescription) {
        this.orderDescription = orderDescription;
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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

    public Long getRefundedAmountFen() {
        return refundedAmountFen;
    }

    public void setRefundedAmountFen(Long refundedAmountFen) {
        this.refundedAmountFen = refundedAmountFen;
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

    public String getSuccessPayPlatform() {
        return successPayPlatform;
    }

    public void setSuccessPayPlatform(String successPayPlatform) {
        this.successPayPlatform = successPayPlatform;
    }

    public String getLatestPaymentOrderNo() {
        return latestPaymentOrderNo;
    }

    public void setLatestPaymentOrderNo(String latestPaymentOrderNo) {
        this.latestPaymentOrderNo = latestPaymentOrderNo;
    }

    public String getLatestChannelOrderNo() {
        return latestChannelOrderNo;
    }

    public void setLatestChannelOrderNo(String latestChannelOrderNo) {
        this.latestChannelOrderNo = latestChannelOrderNo;
    }

    public Instant getPaidTime() {
        return paidTime;
    }

    public void setPaidTime(Instant paidTime) {
        this.paidTime = paidTime;
    }

    public Instant getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(Instant expiredTime) {
        this.expiredTime = expiredTime;
    }

    public Instant getClosedTime() {
        return closedTime;
    }

    public void setClosedTime(Instant closedTime) {
        this.closedTime = closedTime;
    }

    public Instant getCancelledTime() {
        return cancelledTime;
    }

    public void setCancelledTime(Instant cancelledTime) {
        this.cancelledTime = cancelledTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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
