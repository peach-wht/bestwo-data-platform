package com.bestwo.dataplatform.warehouse.source.mapper;

import com.bestwo.dataplatform.warehouse.source.model.BizPaymentNotifyLogSourceRow;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SourceBizPaymentNotifyLogMapper {

    @Select("""
        SELECT
            id,
            notify_log_id,
            notify_id,
            platform,
            notify_type,
            payment_order_id,
            payment_order_no,
            order_id,
            order_no,
            channel_order_no,
            event_type,
            event_status,
            summary,
            request_headers_json::text AS request_headers_json,
            request_body,
            encrypt_type,
            resource_ciphertext,
            resource_nonce,
            resource_associated_data,
            signature_serial_no,
            signature_value,
            process_status,
            process_message,
            processed_at,
            received_at,
            ext_json::text AS ext_json
        FROM biz_payment_notify_log
        WHERE id > #{lastId}
        ORDER BY id ASC
        LIMIT #{limit}
        """)
    List<BizPaymentNotifyLogSourceRow> selectBatchAfterId(@Param("lastId") long lastId, @Param("limit") int limit);
}
