package com.bestwo.dataplatform.order.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bestwo.dataplatform.order.mybatis.JsonbStringTypeHandler;
import java.time.Instant;

@TableName(value = "biz_order_item", autoResultMap = true)
public class BizOrderItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String orderItemId;
    private String orderId;
    private String orderNo;
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
    @TableField(typeHandler = JsonbStringTypeHandler.class)
    private String extJson;
    private Instant createdAt;
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
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

    public String getExtJson() {
        return extJson;
    }

    public void setExtJson(String extJson) {
        this.extJson = extJson;
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
