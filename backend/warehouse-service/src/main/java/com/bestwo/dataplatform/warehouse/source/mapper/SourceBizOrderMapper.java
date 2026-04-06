package com.bestwo.dataplatform.warehouse.source.mapper;

import com.bestwo.dataplatform.warehouse.source.model.BizOrderSourceRow;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SourceBizOrderMapper {

    @Select("""
        SELECT
            id,
            order_id,
            order_no,
            external_order_no,
            biz_type,
            order_source,
            order_title,
            order_description,
            buyer_id,
            buyer_nickname,
            currency,
            total_amount_fen,
            payable_amount_fen,
            paid_amount_fen,
            refunded_amount_fen,
            order_status,
            pay_status,
            preferred_pay_platform,
            preferred_trade_type,
            success_pay_platform,
            latest_payment_order_no,
            latest_channel_order_no,
            paid_time,
            expired_time,
            closed_time,
            cancelled_time,
            remark,
            ext_json::text AS ext_json,
            version,
            created_by,
            updated_by,
            created_at,
            updated_at
        FROM biz_order
        WHERE id > #{lastId}
        ORDER BY id ASC
        LIMIT #{limit}
        """)
    List<BizOrderSourceRow> selectBatchAfterId(@Param("lastId") long lastId, @Param("limit") int limit);
}
