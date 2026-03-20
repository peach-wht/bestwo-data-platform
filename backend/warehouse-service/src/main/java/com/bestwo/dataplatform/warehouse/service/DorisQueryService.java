package com.bestwo.dataplatform.warehouse.service;

import com.bestwo.dataplatform.common.exception.BusinessException;
import com.bestwo.dataplatform.warehouse.config.DorisProperties;
import com.bestwo.dataplatform.warehouse.dto.OrderDetailResponse;
import com.bestwo.dataplatform.warehouse.dto.OrderQueryRequest;
import com.bestwo.dataplatform.warehouse.dto.OrderSummaryDayResponse;
import com.bestwo.dataplatform.warehouse.dto.SummaryQueryRequest;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DorisQueryService {

    private static final Logger log = LoggerFactory.getLogger(DorisQueryService.class);
    private static final BeanPropertyRowMapper<OrderDetailResponse> ORDER_ROW_MAPPER =
        BeanPropertyRowMapper.newInstance(OrderDetailResponse.class);
    private static final BeanPropertyRowMapper<OrderSummaryDayResponse> SUMMARY_ROW_MAPPER =
        BeanPropertyRowMapper.newInstance(OrderSummaryDayResponse.class);

    private final JdbcTemplate jdbcTemplate;
    private final DorisProperties dorisProperties;

    public DorisQueryService(JdbcTemplate dorisJdbcTemplate, DorisProperties dorisProperties) {
        this.jdbcTemplate = dorisJdbcTemplate;
        this.dorisProperties = dorisProperties;
    }

    public Map<String, Object> ping() {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("result", result);
        response.put("database", dorisProperties.getDatabase());
        return response;
    }

    public List<Map<String, Object>> queryTestOrders() {
        String sql = "SELECT * FROM " + qualifiedTable("ods_wx_order") + " LIMIT 10";
        return jdbcTemplate.queryForList(sql);
    }

    public List<OrderDetailResponse> queryOrders(OrderQueryRequest request) {
        OrderTableSpec tableSpec = resolveOrderTableSpec();
        SqlWithArgs sqlWithArgs = buildOrderListSql(request, tableSpec);
        return jdbcTemplate.query(sqlWithArgs.sql(), ORDER_ROW_MAPPER, sqlWithArgs.args().toArray());
    }

    public long countOrders(OrderQueryRequest request) {
        OrderTableSpec tableSpec = resolveOrderTableSpec();
        SqlWithArgs sqlWithArgs = buildOrderCountSql(request, tableSpec);
        Number total = jdbcTemplate.queryForObject(sqlWithArgs.sql(), Number.class, sqlWithArgs.args().toArray());
        return total == null ? 0L : total.longValue();
    }

    public List<OrderSummaryDayResponse> queryDaySummary(SummaryQueryRequest request) {
        SummaryTableSpec tableSpec = resolveSummaryTableSpec();
        SqlWithArgs sqlWithArgs = buildDaySummarySql(request, tableSpec);
        return jdbcTemplate.query(sqlWithArgs.sql(), SUMMARY_ROW_MAPPER, sqlWithArgs.args().toArray());
    }

    private SqlWithArgs buildOrderListSql(OrderQueryRequest request, OrderTableSpec tableSpec) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
            .append(tableSpec.orderIdSelect()).append(", ")
            .append(tableSpec.orderNoSelect()).append(", ")
            .append(tableSpec.orderStatusSelect()).append(", ")
            .append(tableSpec.payStatusSelect()).append(", ")
            .append(tableSpec.orderTimeSelect()).append(", ")
            .append(tableSpec.payTimeSelect()).append(", ")
            .append(tableSpec.totalAmountSelect()).append(", ")
            .append(tableSpec.buyerNicknameSelect())
            .append(" FROM ").append(tableSpec.tableName());

        List<Object> args = appendOrderFilters(sql, request, tableSpec);
        sql.append(" ORDER BY ").append(tableSpec.orderTimeColumn()).append(" DESC");
        sql.append(" LIMIT ? OFFSET ?");
        args.add(request.getPageSize());
        args.add((long) (request.getPageNum() - 1) * request.getPageSize());
        return new SqlWithArgs(sql.toString(), args);
    }

    private SqlWithArgs buildOrderCountSql(OrderQueryRequest request, OrderTableSpec tableSpec) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(1) FROM ").append(tableSpec.tableName());
        List<Object> args = appendOrderFilters(sql, request, tableSpec);
        return new SqlWithArgs(sql.toString(), args);
    }

    private List<Object> appendOrderFilters(StringBuilder sql, OrderQueryRequest request, OrderTableSpec tableSpec) {
        List<Object> args = new ArrayList<>();
        sql.append(" WHERE ").append(tableSpec.orderTimeColumn()).append(" >= ?");
        sql.append(" AND ").append(tableSpec.orderTimeColumn()).append(" < ?");
        args.add(Timestamp.valueOf(request.getStartDate().atStartOfDay()));
        args.add(Timestamp.valueOf(request.getEndDate().plusDays(1).atStartOfDay()));

        String keyword = normalizeKeyword(request.getKeyword());
        if (keyword != null) {
            sql.append(" AND ").append(tableSpec.orderNoColumn()).append(" LIKE ?");
            args.add("%" + keyword + "%");
        }
        return args;
    }

    private SqlWithArgs buildDaySummarySql(SummaryQueryRequest request, SummaryTableSpec tableSpec) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
            .append(tableSpec.statDateSelect()).append(", ")
            .append(tableSpec.orderCountSelect()).append(", ")
            .append(tableSpec.paidOrderCountSelect()).append(", ")
            .append(tableSpec.totalAmountSelect()).append(", ")
            .append(tableSpec.paidAmountSelect()).append(", ")
            .append(tableSpec.refundAmountSelect())
            .append(" FROM ").append(tableSpec.tableName())
            .append(" WHERE ").append(tableSpec.statDateColumn()).append(" >= ?")
            .append(" AND ").append(tableSpec.statDateColumn()).append(" <= ?")
            .append(" ORDER BY ").append(tableSpec.statDateColumn()).append(" ASC");

        List<Object> args = new ArrayList<>();
        args.add(Date.valueOf(request.getStartDate()));
        args.add(Date.valueOf(request.getEndDate()));
        return new SqlWithArgs(sql.toString(), args);
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
            qualifiedTable("ads_order_day_summary"),
            statDateColumn,
            stringSelect(statDateColumn, "statDate"),
            numericSelect(columns, List.of("order_count", "total_order_count"), "orderCount", true),
            numericSelect(columns, List.of("paid_order_count", "success_order_count"), "paidOrderCount", false),
            numericSelect(columns, List.of("total_amount", "order_amount", "total_order_amount"), "totalAmount", false),
            numericSelect(columns, List.of("paid_amount", "total_paid_amount"), "paidAmount", false),
            numericSelect(columns, List.of("refund_amount", "total_refund_amount"), "refundAmount", false)
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
            qualifiedTable(tableName),
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
        String sql = "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";
        List<String> columns = jdbcTemplate.query(
            sql,
            (rs, rowNum) -> rs.getString("COLUMN_NAME").toLowerCase(Locale.ROOT),
            dorisProperties.getDatabase(),
            tableName
        );
        return new LinkedHashSet<>(columns);
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

    private String qualifiedTable(String tableName) {
        return dorisProperties.getDatabase() + "." + tableName;
    }

    private record SqlWithArgs(String sql, List<Object> args) {
    }

    private record OrderTableSpec(
        String tableName,
        String orderNoColumn,
        String orderTimeColumn,
        String orderIdSelect,
        String orderNoSelect,
        String orderStatusSelect,
        String payStatusSelect,
        String orderTimeSelect,
        String payTimeSelect,
        String totalAmountSelect,
        String buyerNicknameSelect
    ) {
    }

    private record SummaryTableSpec(
        String tableName,
        String statDateColumn,
        String statDateSelect,
        String orderCountSelect,
        String paidOrderCountSelect,
        String totalAmountSelect,
        String paidAmountSelect,
        String refundAmountSelect
    ) {
    }
}
