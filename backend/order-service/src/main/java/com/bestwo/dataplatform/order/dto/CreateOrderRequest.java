package com.bestwo.dataplatform.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateOrderRequest {

    @Size(max = 64, message = "externalOrderNo length must be <= 64")
    private String externalOrderNo;

    @NotBlank(message = "orderTitle is required")
    @Size(max = 128, message = "orderTitle length must be <= 128")
    private String orderTitle;

    @Size(max = 255, message = "orderDescription length must be <= 255")
    private String orderDescription;

    @Size(max = 64, message = "buyerId length must be <= 64")
    private String buyerId;

    @Size(max = 128, message = "buyerNickname length must be <= 128")
    private String buyerNickname;

    @Min(value = 1, message = "totalAmountFen must be greater than 0")
    private Long totalAmountFen;

    @Min(value = 1, message = "payableAmountFen must be greater than 0")
    private Long payableAmountFen;

    @Size(max = 32, message = "preferredPayPlatform length must be <= 32")
    private String preferredPayPlatform;

    @Size(max = 32, message = "preferredTradeType length must be <= 32")
    private String preferredTradeType;

    @Size(max = 255, message = "remark length must be <= 255")
    private String remark;

    private String extJson;

    @Size(max = 64, message = "createdBy length must be <= 64")
    private String createdBy;

    public String getExternalOrderNo() {
        return externalOrderNo;
    }

    public void setExternalOrderNo(String externalOrderNo) {
        this.externalOrderNo = externalOrderNo;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
