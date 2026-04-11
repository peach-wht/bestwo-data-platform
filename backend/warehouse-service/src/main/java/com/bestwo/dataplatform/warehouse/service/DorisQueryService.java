package com.bestwo.dataplatform.warehouse.service;

import com.bestwo.dataplatform.common.exception.BusinessException;
import com.bestwo.dataplatform.warehouse.config.DorisProperties;
import com.bestwo.dataplatform.warehouse.dto.OrderDetailResponse;
import com.bestwo.dataplatform.warehouse.dto.OrderQueryRequest;
import com.bestwo.dataplatform.warehouse.dto.OrderSummaryDayResponse;
import com.bestwo.dataplatform.warehouse.dto.PayOverviewQueryRequest;
import com.bestwo.dataplatform.warehouse.dto.PayOverviewResponse;
import com.bestwo.dataplatform.warehouse.dto.PayTrendResponse;
import com.bestwo.dataplatform.warehouse.dto.QualityResultResponse;
import com.bestwo.dataplatform.warehouse.dto.SummaryQueryRequest;
import com.bestwo.dataplatform.warehouse.dto.SyncJobLogResponse;
import com.bestwo.dataplatform.warehouse.mapper.WarehouseDorisMapper;
import com.bestwo.dataplatform.warehouse.mapper.model.OrderTableSpec;
import com.bestwo.dataplatform.warehouse.mapper.model.PayTrendTableSpec;
import com.bestwo.dataplatform.warehouse.mapper.model.QualityResultTableSpec;
import com.bestwo.dataplatform.warehouse.mapper.model.SummaryTableSpec;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DorisQueryService {

    private static final Logger log = LoggerFactory.getLogger(DorisQueryService.class);

    private final WarehouseDorisMapper warehouseDorisMapper;
    private final DorisProperties dorisProperties;

    public DorisQueryService(WarehouseDorisMapper warehouseDorisMapper, DorisProperties dorisProperties) {
        this.warehouseDorisMapper = warehouseDorisMapper;
        this.dorisProperties = dorisProperties;
    }

    public Map<String, Object> ping() {
        Integer result = warehouseDorisMapper.ping();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("result", result);
        response.put("database", dorisProperties.getDatabase());
        return response;
    }

    public List<Map<String, Object>> queryTestOrders() {
        return warehouseDorisMapper.queryTestOrders();
    }

    public List<OrderDetailResponse> queryOrders(OrderQueryRequest request) {
        OrderTableSpec tableSpec = resolveOrderTableSpec();
        return warehouseDorisMapper.queryOrders(
            tableSpec,
            toStartTimestamp(request.getStartDate()),
            toEndTimestamp(request.getEndDate()),
            normalizeKeyword(request.getKeyword()),
            request.getPageSize(),
            (long) (request.getPageNum() - 1) * request.getPageSize()
        );
    }

    public long countOrders(OrderQueryRequest request) {
        OrderTableSpec tableSpec = resolveOrderTableSpec();
        Long total = warehouseDorisMapper.countOrders(
            tableSpec,
            toStartTimestamp(request.getStartDate()),
            toEndTimestamp(request.getEndDate()),
            normalizeKeyword(request.getKeyword())
        );
        return total == null ? 0L : total;
    }

    public List<OrderSummaryDayResponse> queryDaySummary(SummaryQueryRequest request) {
        SummaryTableSpec tableSpec = resolveSummaryTableSpec();
        return warehouseDorisMapper.queryDaySummary(tableSpec, request.getStartDate(), request.getEndDate());
    }

    public PayOverviewResponse queryPayOverview(PayOverviewQueryRequest request) {
        try {
            if (!request.hasDateRange() && tableExists("ads_pay_dashboard_overview")) {
                PayOverviewResponse response = warehouseDorisMapper.queryAdsPayOverview("ALL");
                if (response != null) {
                    return normalizePayOverview(response);
                }
            }

            if (!hasPayTrendSourceTable()) {
                log.warn("no available pay overview source table found in Doris, fallback to empty overview");
                return normalizePayOverview(null);
            }

            PayTrendTableSpec tableSpec = resolvePayTrendTableSpec();
            PayOverviewResponse response = warehouseDorisMapper.queryPayOverviewFromTrend(
                tableSpec,
                request.getStartDate(),
                request.getEndDate()
            );
            return normalizePayOverview(response);
        } catch (RuntimeException exception) {
            throw new BusinessException(resolveExceptionMessage("failed to query pay overview from Doris", exception));
        }
    }

    public List<PayTrendResponse> queryPayTrend(SummaryQueryRequest request) {
        try {
            if (!hasPayTrendSourceTable()) {
                log.warn("no available pay trend table found in Doris, fallback to empty trend list");
                return Collections.emptyList();
            }
            PayTrendTableSpec tableSpec = resolvePayTrendTableSpec();
            return warehouseDorisMapper.queryPayTrend(tableSpec, request.getStartDate(), request.getEndDate());
        } catch (RuntimeException exception) {
            throw new BusinessException(resolveExceptionMessage("failed to query pay trend from Doris", exception));
        }
    }

    public List<SyncJobLogResponse> queryRecentJobLogs(String jobCode, int limit) {
        if (!tableExists("dw_sync_job_log")) {
            return Collections.emptyList();
        }
        return warehouseDorisMapper.queryRecentSyncJobLogs(jobCode, limit);
    }

    public List<QualityResultResponse> queryQualityResults(int limit) {
        if (!tableExists("dw_quality_result")) {
            return Collections.emptyList();
        }
        return warehouseDorisMapper.queryQualityResults(resolveQualityResultTableSpec(), limit);
    }

    private OrderTableSpec resolveOrderTableSpec() {
        OrderTableSpec dwdTableSpec = buildOrderTableSpec("dwd_wx_order_detail");
        if (dwdTableSpec != null) {
            return dwdTableSpec;
        }

        log.warn("dwd_wx_order_detail not found, fallback to ods_wx_order");
        OrderTableSpec odsTableSpec = buildOrderTableSpec("ods_wx_order");
        if (odsTableSpec != null) {
            return odsTableSpec;
        }
        throw new BusinessException("no available order detail table found in Doris");
    }

    private SummaryTableSpec resolveSummaryTableSpec() {
        Set<String> columns = getTableColumns("ads_order_day_summary");
        if (columns.isEmpty()) {
            throw new BusinessException("ads_order_day_summary table not found in Doris");
        }

        String statDateColumn = resolveRequiredColumn(
            columns,
            List.of("stat_date", "order_date", "dt"),
            "stat_date"
        );

        return new SummaryTableSpec(
            "ads_order_day_summary",
            statDateColumn,
            numericExpression(columns, List.of("order_count", "total_order_count", "order_cnt"), true),
            numericExpression(columns, List.of("paid_order_count", "success_order_count", "paid_order_cnt"), false),
            numericExpression(columns, List.of("total_amount", "order_amount", "total_order_amount"), false),
            numericExpression(columns, List.of("paid_amount", "total_paid_amount", "total_pay_amount"), false),
            numericExpression(columns, List.of("refund_amount", "total_refund_amount", "refund_pay_amount"), false)
        );
    }

    private PayTrendTableSpec resolvePayTrendTableSpec() {
        Set<String> dwsColumns = getTableColumns("dws_wx_pay_trade_day");
        if (!dwsColumns.isEmpty()) {
            String statDateColumn = resolveRequiredColumn(dwsColumns, List.of("stat_date", "dt"), "stat_date");
            String orderCountExpression = numericExpression(dwsColumns, List.of("order_count", "order_cnt"), true);
            String paidOrderCountExpression = numericExpression(dwsColumns, List.of("paid_order_count", "paid_order_cnt"), false);
            return new PayTrendTableSpec(
                "dws_wx_pay_trade_day",
                statDateColumn,
                orderCountExpression,
                paidOrderCountExpression,
                numericExpression(dwsColumns, List.of("unpaid_order_count", "unpaid_order_cnt"), false),
                numericExpression(dwsColumns, List.of("closed_order_count", "cancelled_order_cnt"), false),
                numericExpression(dwsColumns, List.of("total_amount", "order_amount", "total_order_amount"), false),
                numericExpression(dwsColumns, List.of("paid_amount", "total_paid_amount", "total_pay_amount"), false),
                numericExpression(dwsColumns, List.of("refund_amount", "total_refund_amount", "refund_pay_amount"), false),
                numericExpression(
                    dwsColumns,
                    List.of("pay_success_rate"),
                    false,
                    buildRatioExpression(paidOrderCountExpression, orderCountExpression)
                )
            );
        }

        log.warn("dws_wx_pay_trade_day not found, fallback to ads_order_day_summary");
        Set<String> adsColumns = getTableColumns("ads_order_day_summary");
        if (adsColumns.isEmpty()) {
            throw new BusinessException("no available pay trend table found in Doris");
        }

        String statDateColumn = resolveRequiredColumn(adsColumns, List.of("stat_date", "order_date", "dt"), "stat_date");
        String orderCountExpression = numericExpression(adsColumns, List.of("order_count", "total_order_count", "order_cnt"), true);
        String paidOrderCountExpression = numericExpression(adsColumns, List.of("paid_order_count", "success_order_count", "paid_order_cnt"), false);
        return new PayTrendTableSpec(
            "ads_order_day_summary",
            statDateColumn,
            orderCountExpression,
            paidOrderCountExpression,
            numericExpression(adsColumns, List.of("unpaid_order_count", "unpaid_order_cnt"), false),
            numericExpression(adsColumns, List.of("closed_order_count", "cancelled_order_cnt"), false),
            numericExpression(adsColumns, List.of("total_amount", "order_amount", "total_order_amount"), false),
            numericExpression(adsColumns, List.of("paid_amount", "total_paid_amount", "total_pay_amount"), false),
            numericExpression(adsColumns, List.of("refund_amount", "total_refund_amount", "refund_pay_amount"), false),
            buildRatioExpression(paidOrderCountExpression, orderCountExpression)
        );
    }

    private QualityResultTableSpec resolveQualityResultTableSpec() {
        Set<String> columns = getTableColumns("dw_quality_result");
        if (columns.isEmpty()) {
            throw new BusinessException("dw_quality_result table not found in Doris");
        }

        String checkedAtOrderExpression = resolveOptionalColumn(
            columns,
            List.of("checked_at", "check_time", "finished_at", "created_at")
        );

        return new QualityResultTableSpec(
            "dw_quality_result",
            stringExpression(columns, List.of("rule_code", "quality_rule_code"), false),
            stringExpression(columns, List.of("rule_name", "quality_rule_name"), false),
            stringExpression(columns, List.of("table_name", "target_table", "source_table"), false),
            stringExpression(columns, List.of("result_status", "check_status", "run_status"), false),
            stringExpression(columns, List.of("result_level", "severity", "alert_level"), false),
            numericExpression(columns, List.of("failed_count", "failed_rows", "error_count"), false),
            numericExpression(columns, List.of("total_count", "total_rows", "checked_rows"), false),
            stringExpression(columns, List.of("message", "result_message", "remark"), false),
            stringExpression(columns, List.of("checked_at", "check_time", "finished_at", "created_at"), false),
            checkedAtOrderExpression == null ? "1" : checkedAtOrderExpression
        );
    }

    private OrderTableSpec buildOrderTableSpec(String tableName) {
        Set<String> columns = getTableColumns(tableName);
        if (columns.isEmpty()) {
            return null;
        }

        String orderIdColumn = resolveRequiredColumn(columns, List.of("order_id"), "order_id");
        String orderNoColumn = resolveRequiredColumn(columns, List.of("order_no"), "order_no");
        String orderTimeColumn = resolveRequiredColumn(
            columns,
            List.of("order_time", "create_time", "gmt_create"),
            "order_time"
        );

        return new OrderTableSpec(
            tableName,
            orderNoColumn,
            orderTimeColumn,
            stringSelect(orderIdColumn, "orderId"),
            stringSelect(orderNoColumn, "orderNo"),
            stringSelect(columns, List.of("order_status"), "orderStatus", false),
            stringSelect(columns, List.of("pay_status"), "payStatus", false),
            stringSelect(orderTimeColumn, "orderTime"),
            stringSelect(columns, List.of("pay_time", "payment_time"), "payTime", false),
            numericSelect(columns, List.of("total_amount", "order_amount", "pay_amount"), "totalAmount", false),
            stringSelect(
                columns,
                List.of("buyer_nickname", "nickname", "user_nickname", "buyer_name"),
                "buyerNickname",
                false
            )
        );
    }

    private Set<String> getTableColumns(String tableName) {
        List<String> columns = warehouseDorisMapper.listTableColumns(dorisProperties.getDatabase(), tableName);
        Set<String> normalizedColumns = new LinkedHashSet<>();
        for (String column : columns) {
            normalizedColumns.add(column.toLowerCase(Locale.ROOT));
        }
        return normalizedColumns;
    }

    private String resolveRequiredColumn(Set<String> columns, List<String> candidates, String fieldName) {
        for (String candidate : candidates) {
            if (columns.contains(candidate.toLowerCase(Locale.ROOT))) {
                return candidate;
            }
        }
        throw new BusinessException("required field not found in Doris table: " + fieldName);
    }

    private String stringSelect(Set<String> columns, List<String> candidates, String alias, boolean required) {
        String column = resolveOptionalColumn(columns, candidates);
        if (column == null) {
            if (required) {
                throw new BusinessException("required field not found in Doris table: " + alias);
            }
            return "NULL AS " + alias;
        }
        return stringSelect(column, alias);
    }

    private String stringSelect(String column, String alias) {
        return "CAST(" + column + " AS CHAR) AS " + alias;
    }

    private String stringExpression(Set<String> columns, List<String> candidates, boolean required) {
        String column = resolveOptionalColumn(columns, candidates);
        if (column == null) {
            if (required) {
                throw new BusinessException("required field not found in Doris table");
            }
            return "NULL";
        }
        return "CAST(" + column + " AS CHAR)";
    }

    private String numericSelect(Set<String> columns, List<String> candidates, String alias, boolean required) {
        String column = resolveOptionalColumn(columns, candidates);
        if (column == null) {
            if (required) {
                throw new BusinessException("required field not found in Doris table: " + alias);
            }
            return "0 AS " + alias;
        }
        return column + " AS " + alias;
    }

    private String numericExpression(Set<String> columns, List<String> candidates, boolean required) {
        return numericExpression(columns, candidates, required, "0");
    }

    private String numericExpression(Set<String> columns, List<String> candidates, boolean required, String defaultExpression) {
        String column = resolveOptionalColumn(columns, candidates);
        if (column == null) {
            if (required) {
                throw new BusinessException("required numeric field not found in Doris table");
            }
            return defaultExpression;
        }
        return column;
    }

    private String buildRatioExpression(String numeratorExpression, String denominatorExpression) {
        return "CASE WHEN " + denominatorExpression
            + " = 0 THEN 0 ELSE ROUND(CAST("
            + numeratorExpression
            + " AS DECIMAL(18, 4)) / "
            + denominatorExpression
            + ", 4) END";
    }

    private String resolveOptionalColumn(Set<String> columns, List<String> candidates) {
        for (String candidate : candidates) {
            if (columns.contains(candidate.toLowerCase(Locale.ROOT))) {
                return candidate;
            }
        }
        return null;
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String normalized = keyword.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private Timestamp toStartTimestamp(LocalDate startDate) {
        return Timestamp.valueOf(startDate.atStartOfDay());
    }

    private Timestamp toEndTimestamp(LocalDate endDate) {
        return Timestamp.valueOf(endDate.plusDays(1).atStartOfDay());
    }

    private boolean tableExists(String tableName) {
        return !getTableColumns(tableName).isEmpty();
    }

    private boolean hasPayTrendSourceTable() {
        return tableExists("dws_wx_pay_trade_day") || tableExists("ads_order_day_summary");
    }

    private String resolveExceptionMessage(String fallbackMessage, Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && !message.isBlank()) {
                return fallbackMessage + ": " + message;
            }
            current = current.getCause();
        }
        return fallbackMessage;
    }

    private PayOverviewResponse normalizePayOverview(PayOverviewResponse response) {
        PayOverviewResponse normalized = response == null ? new PayOverviewResponse() : response;
        if (normalized.getOrderCount() == null) {
            normalized.setOrderCount(0L);
        }
        if (normalized.getPaidOrderCount() == null) {
            normalized.setPaidOrderCount(0L);
        }
        if (normalized.getUnpaidOrderCount() == null) {
            normalized.setUnpaidOrderCount(0L);
        }
        if (normalized.getClosedOrderCount() == null) {
            normalized.setClosedOrderCount(0L);
        }
        if (normalized.getTotalAmount() == null) {
            normalized.setTotalAmount(BigDecimal.ZERO);
        }
        if (normalized.getPaidAmount() == null) {
            normalized.setPaidAmount(BigDecimal.ZERO);
        }
        if (normalized.getRefundAmount() == null) {
            normalized.setRefundAmount(BigDecimal.ZERO);
        }
        if (normalized.getPaySuccessRate() == null) {
            normalized.setPaySuccessRate(BigDecimal.ZERO);
        }
        return normalized;
    }
}
