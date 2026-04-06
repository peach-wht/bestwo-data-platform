package com.bestwo.dataplatform.order.dto;

import java.time.Instant;

public class OrderItemResponse {

    private String orderItemId;
    private Integer lineNo;
    private String itemType;
    private String productId;
    private String productCode;
    private String productName;
    private String skuId;
    private String skuCode;
    private String skuName;
    private Long quantity;
    private String currency;
    private Long unitPriceFen;
    private Long originalAmountFen;
    private Long discountAmountFen;
    private Long payableAmountFen;
    private Long paidAmountFen;
    private Long refundAmountFen;
    private String itemStatus;
    private String remark;
    private Instant createdAt;

    public String getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Integer getLineNo() {
        return lineNo;
    }

    public void setLineNo(Integer lineNo) {
        this.lineNo = lineNo;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Long getUnitPriceFen() {
        return unitPriceFen;
    }

    public void setUnitPriceFen(Long unitPriceFen) {
        this.unitPriceFen = unitPriceFen;
    }

    public Long getOriginalAmountFen() {
        return originalAmountFen;
    }

    public void setOriginalAmountFen(Long originalAmountFen) {
        this.originalAmountFen = originalAmountFen;
    }

    public Long getDiscountAmountFen() {
        return discountAmountFen;
    }

    public void setDiscountAmountFen(Long discountAmountFen) {
        this.discountAmountFen = discountAmountFen;
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

    public Long getRefundAmountFen() {
        return refundAmountFen;
    }

    public void setRefundAmountFen(Long refundAmountFen) {
        this.refundAmountFen = refundAmountFen;
    }

    public String getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
