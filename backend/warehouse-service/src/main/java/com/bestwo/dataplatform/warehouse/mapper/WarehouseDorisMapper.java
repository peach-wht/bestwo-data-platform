package com.bestwo.dataplatform.warehouse.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bestwo.dataplatform.warehouse.dto.JobDefinitionResponse;
import com.bestwo.dataplatform.warehouse.dto.JobExecutionLogResponse;
import com.bestwo.dataplatform.warehouse.dto.MetadataColumnResponse;
import com.bestwo.dataplatform.warehouse.dto.MetadataDatasourceResponse;
import com.bestwo.dataplatform.warehouse.dto.MetadataTableColumnSnapshot;
import com.bestwo.dataplatform.warehouse.dto.MetadataTableResponse;
import com.bestwo.dataplatform.warehouse.dto.PayOverviewResponse;
import com.bestwo.dataplatform.warehouse.dto.PayTrendResponse;
import com.bestwo.dataplatform.warehouse.dto.OrderDetailResponse;
import com.bestwo.dataplatform.warehouse.dto.OrderSummaryDayResponse;
import com.bestwo.dataplatform.warehouse.dto.QualityResultResponse;
import com.bestwo.dataplatform.warehouse.dto.SyncJobLogResponse;
import com.bestwo.dataplatform.warehouse.entity.DwJobLogEntity;
import com.bestwo.dataplatform.warehouse.entity.DwMetaColumnEntity;
import com.bestwo.dataplatform.warehouse.entity.DwMetaDatasourceEntity;
import com.bestwo.dataplatform.warehouse.entity.DwMetaTableEntity;
import com.bestwo.dataplatform.warehouse.entity.DwSyncJobEntity;
import com.bestwo.dataplatform.warehouse.entity.DwSyncJobLogEntity;
import com.bestwo.dataplatform.warehouse.entity.OdsWxOrderEntity;
import com.bestwo.dataplatform.warehouse.mapper.model.OrderTableSpec;
import com.bestwo.dataplatform.warehouse.mapper.model.PayTrendTableSpec;
import com.bestwo.dataplatform.warehouse.mapper.model.QualityResultTableSpec;
import com.bestwo.dataplatform.warehouse.mapper.model.SummaryTableSpec;
import com.bestwo.dataplatform.warehouse.source.model.BizOrderSourceRow;
import com.bestwo.dataplatform.warehouse.source.model.BizPaymentNotifyLogSourceRow;
import com.bestwo.dataplatform.warehouse.source.model.BizPaymentOrderSourceRow;
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

    @Select("""
        SELECT
            TABLE_NAME AS tableName,
            COLUMN_NAME AS columnName,
            DATA_TYPE AS dataType,
            IS_NULLABLE AS isNullable,
            ORDINAL_POSITION AS ordinalPosition,
            COLUMN_COMMENT AS columnComment
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = #{database}
          AND TABLE_NAME = #{tableName}
        ORDER BY ORDINAL_POSITION
        """)
    List<MetadataTableColumnSnapshot> listTableColumnDetails(
        @Param("database") String database,
        @Param("tableName") String tableName
    );

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

    @Select("""
        SELECT
            CAST(latest_stat_date AS CHAR) AS latestStatDate,
            order_count AS orderCount,
            paid_order_count AS paidOrderCount,
            unpaid_order_count AS unpaidOrderCount,
            0 AS closedOrderCount,
            total_amount AS totalAmount,
            paid_amount AS paidAmount,
            refund_amount AS refundAmount,
            pay_success_rate AS paySuccessRate
        FROM ads_pay_dashboard_overview
        WHERE metric_scope = #{metricScope}
        LIMIT 1
        """)
    PayOverviewResponse queryAdsPayOverview(@Param("metricScope") String metricScope);

    PayOverviewResponse queryPayOverviewFromTrend(
        @Param("tableSpec") PayTrendTableSpec tableSpec,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    List<PayTrendResponse> queryPayTrend(
        @Param("tableSpec") PayTrendTableSpec tableSpec,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    int insertOdsWxOrders(@Param("list") List<BizOrderSourceRow> list);

    int insertOdsWxPaymentOrders(@Param("list") List<BizPaymentOrderSourceRow> list);

    int insertOdsWxPaymentNotifyLogs(@Param("list") List<BizPaymentNotifyLogSourceRow> list);

    int saveSyncJob(@Param("job") DwSyncJobEntity job);

    int saveSyncJobLog(@Param("log") DwSyncJobLogEntity log);

    int saveDwJobLog(@Param("log") DwJobLogEntity log);

    int saveMetaDatasource(@Param("datasource") DwMetaDatasourceEntity datasource);

    int saveMetaTable(@Param("table") DwMetaTableEntity table);

    int saveMetaColumns(@Param("list") List<DwMetaColumnEntity> list);

    List<SyncJobLogResponse> queryLatestSyncJobLogs(@Param("jobCode") String jobCode, @Param("limit") Integer limit);

    List<SyncJobLogResponse> queryRecentSyncJobLogs(@Param("jobCode") String jobCode, @Param("limit") Integer limit);

    List<MetadataDatasourceResponse> queryMetaDatasources();

    List<MetadataTableResponse> queryMetaTables(
        @Param("datasourceCode") String datasourceCode,
        @Param("tableLayer") String tableLayer,
        @Param("limit") Integer limit
    );

    List<MetadataColumnResponse> queryMetaColumns(@Param("tableCode") String tableCode, @Param("limit") Integer limit);

    List<JobDefinitionResponse> queryJobDefinitions(@Param("limit") Integer limit);

    List<JobExecutionLogResponse> queryDwJobLogs(@Param("jobCode") String jobCode, @Param("limit") Integer limit);

    List<QualityResultResponse> queryQualityResults(
        @Param("tableSpec") QualityResultTableSpec tableSpec,
        @Param("limit") Integer limit
    );

    @Select("SELECT COUNT(1) FROM ${tableName}")
    Long countRows(@Param("tableName") String tableName);

    void executeSql(@Param("sql") String sql);

    default List<Map<String, Object>> queryTestOrders() {
        return selectMaps(new QueryWrapper<OdsWxOrderEntity>().last("LIMIT 10"));
    }
}
