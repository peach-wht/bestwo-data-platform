package com.bestwo.dataplatform.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("ods_wx_payment_order")
public class OdsWxPaymentOrderEntity {

    @TableId(value = "payment_order_id", type = IdType.INPUT)
    private String paymentOrderId;

    public String getPaymentOrderId() {
        return paymentOrderId;
    }

    public void setPaymentOrderId(String paymentOrderId) {
        this.paymentOrderId = paymentOrderId;
    }
}
