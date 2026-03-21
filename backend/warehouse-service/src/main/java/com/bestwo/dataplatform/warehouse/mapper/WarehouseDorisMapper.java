package com.bestwo.dataplatform.warehouse.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bestwo.dataplatform.warehouse.dto.OrderDetailResponse;
import com.bestwo.dataplatform.warehouse.dto.OrderSummaryDayResponse;
import com.bestwo.dataplatform.warehouse.entity.OdsWxOrderEntity;
import com.bestwo.dataplatform.warehouse.mapper.model.OrderTableSpec;
import com.bestwo.dataplatform.warehouse.mapper.model.SummaryTableSpec;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WarehouseDorisMapper extends BaseMapper<OdsWxOrderEntity> {

    @Select("SELECT 1")
    Integer ping();

    @Select("""
        SELECT COLUMN_NAME
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = #{database}
          AND TABLE_NAME = #{tableName}
        """)
    List<String> listTableColumns(@Param("database") String database, @Param("tableName") String tableName);

    List<OrderDetailResponse> queryOrders(
        @Param("tableSpec") OrderTableSpec tableSpec,
        @Param("startTime") Timestamp startTime,
        @Param("endTime") Timestamp endTime,
        @Param("keyword") String keyword,
        @Param("limit") Integer limit,
        @Param("offset") Long offset
    );

    Long countOrders(
        @Param("tableSpec") OrderTableSpec tableSpec,
        @Param("startTime") Timestamp startTime,
        @Param("endTime") Timestamp endTime,
        @Param("keyword") String keyword
    );

    List<OrderSummaryDayResponse> queryDaySummary(
        @Param("tableSpec") SummaryTableSpec tableSpec,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    default List<Map<String, Object>> queryTestOrders() {
        return selectMaps(new QueryWrapper<OdsWxOrderEntity>().last("LIMIT 10"));
    }
}
