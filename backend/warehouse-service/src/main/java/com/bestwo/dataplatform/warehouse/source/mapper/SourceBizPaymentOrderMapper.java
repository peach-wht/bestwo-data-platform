package com.bestwo.dataplatform.warehouse.source.mapper;

import com.bestwo.dataplatform.warehouse.source.model.BizPaymentOrderSourceRow;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SourceBizPaymentOrderMapper {

    @Select("""
        SELECT
            id,
            payment_order_id,
            payment_order_no,
            order_id,
            order_no,
            platform,
            trade_type,
            merchant_code,
            channel_app_id,
            channel_merchant_id,
            subject,
            description,
            currency,
            request_amount_fen,
            success_amount_fen,
            payer_id,
            client_ip,
            notify_url,
            channel_order_no,
            channel_prepay_id,
            code_url,
            status,
            fail_code,
            fail_message,
            expired_time,
            success_time,
            closed_time,
            last_notify_time,
            ext_json::text AS ext_json,
            version,
            created_by,
            updated_by,
            created_at,
            updated_at
        FROM biz_payment_order
        WHERE id > #{lastId}
        ORDER BY id ASC
        LIMIT #{limit}
        """)
    List<BizPaymentOrderSourceRow> selectBatchAfterId(@Param("lastId") long lastId, @Param("limit") int limit);
}
