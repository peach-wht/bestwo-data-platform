package com.bestwo.dataplatform.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("ods_wx_payment_notify")
public class OdsWxPaymentNotifyEntity {

    @TableId(value = "notify_log_id", type = IdType.INPUT)
    private String notifyLogId;

    public String getNotifyLogId() {
        return notifyLogId;
    }

    public void setNotifyLogId(String notifyLogId) {
        this.notifyLogId = notifyLogId;
    }
}
