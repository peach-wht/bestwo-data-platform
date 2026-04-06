package com.bestwo.dataplatform.warehouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class PayOverviewResponse {

    @JsonProperty("latest_stat_date")
    private String latestStatDate;

    @JsonProperty("order_count")
    private Long orderCount;

    @JsonProperty("paid_order_count")
    private Long paidOrderCount;

    @JsonProperty("unpaid_order_count")
    private Long unpaidOrderCount;

    @JsonProperty("closed_order_count")
    private Long closedOrderCount;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @JsonProperty("paid_amount")
    private BigDecimal paidAmount;

    @JsonProperty("refund_amount")
    private BigDecimal refundAmount;

    @JsonProperty("pay_success_rate")
    private BigDecimal paySuccessRate;

    public String getLatestStatDate() {
        return latestStatDate;
    }

    public void setLatestStatDate(String latestStatDate) {
        this.latestStatDate = latestStatDate;
    }

    public Long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Long orderCount) {
        this.orderCount = orderCount;
    }

    public Long getPaidOrderCount() {
        return paidOrderCount;
    }

    public void setPaidOrderCount(Long paidOrderCount) {
        this.paidOrderCount = paidOrderCount;
    }

    public Long getUnpaidOrderCount() {
        return unpaidOrderCount;
    }

    public void setUnpaidOrderCount(Long unpaidOrderCount) {
        this.unpaidOrderCount = unpaidOrderCount;
    }

    public Long getClosedOrderCount() {
        return closedOrderCount;
    }

    public void setClosedOrderCount(Long closedOrderCount) {
        this.closedOrderCount = closedOrderCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public BigDecimal getPaySuccessRate() {
        return paySuccessRate;
    }

    public void setPaySuccessRate(BigDecimal paySuccessRate) {
        this.paySuccessRate = paySuccessRate;
    }
}
