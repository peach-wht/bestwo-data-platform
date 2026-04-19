package com.bestwo.dataplatform.warehouse.controller;

import com.bestwo.dataplatform.common.api.ApiResponse;
import com.bestwo.dataplatform.warehouse.dto.OrderPageResponse;
import com.bestwo.dataplatform.warehouse.dto.OrderQueryRequest;
import com.bestwo.dataplatform.warehouse.dto.OrderSummaryDayResponse;
import com.bestwo.dataplatform.warehouse.dto.PayOverviewQueryRequest;
import com.bestwo.dataplatform.warehouse.dto.PayOverviewResponse;
import com.bestwo.dataplatform.warehouse.dto.PayTrendResponse;
import com.bestwo.dataplatform.warehouse.dto.QualityResultResponse;
import com.bestwo.dataplatform.warehouse.dto.SummaryQueryRequest;
import com.bestwo.dataplatform.warehouse.dto.SyncJobLogResponse;
import com.bestwo.dataplatform.warehouse.service.DorisQueryService;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/warehouse")
public class WarehouseController {

    private static final Logger log = LoggerFactory.getLogger(WarehouseController.class);

    private final DorisQueryService dorisQueryService;

    public WarehouseController(DorisQueryService dorisQueryService) {
        this.dorisQueryService = dorisQueryService;
    }

    @GetMapping("/ping")
    public ApiResponse<Map<String, Object>> ping() {
        return ApiResponse.success(dorisQueryService.ping());
    }

    @GetMapping("/orders/test")
    public ApiResponse<List<Map<String, Object>>> testOrders() {
        return ApiResponse.success(dorisQueryService.queryTestOrders());
    }

    @GetMapping("/orders")
    public ApiResponse<OrderPageResponse> queryOrders(@Valid @ModelAttribute OrderQueryRequest request) {
        long startedAtNanos = System.nanoTime();
        log.info("warehouse orders query started {}", StructuredArguments.entries(buildOrderQueryFields(
            "warehouse_orders_query_started",
            request,
            null
        )));
        OrderPageResponse response = new OrderPageResponse();
        response.setList(dorisQueryService.queryOrders(request));
        response.setPageNum(request.getPageNum());
        response.setPageSize(request.getPageSize());
        response.setTotal(dorisQueryService.countOrders(request));
        log.info("warehouse orders query completed {}", StructuredArguments.entries(buildOrderQueryFields(
            "warehouse_orders_query_completed",
            request,
            Map.of(
                "resultSize", response.getList() == null ? 0 : response.getList().size(),
                "total", response.getTotal(),
                "durationMs", (System.nanoTime() - startedAtNanos) / 1_000_000L
            )
        )));
        return ApiResponse.success(response);
    }

    @GetMapping("/summary/day")
    public ApiResponse<List<OrderSummaryDayResponse>> queryDaySummary(
        @Valid @ModelAttribute SummaryQueryRequest request
    ) {
        long startedAtNanos = System.nanoTime();
        log.info("warehouse day summary query started {}", StructuredArguments.entries(buildSummaryQueryFields(
            "warehouse_day_summary_query_started",
            request,
            null
        )));
        List<OrderSummaryDayResponse> response = dorisQueryService.queryDaySummary(request);
        log.info("warehouse day summary query completed {}", StructuredArguments.entries(buildSummaryQueryFields(
            "warehouse_day_summary_query_completed",
            request,
            Map.of(
                "resultSize", response.size(),
                "durationMs", (System.nanoTime() - startedAtNanos) / 1_000_000L
            )
        )));
        return ApiResponse.success(response);
    }

    @GetMapping("/pay/overview")
    public ApiResponse<PayOverviewResponse> queryPayOverview(@Valid @ModelAttribute PayOverviewQueryRequest request) {
        long startedAtNanos = System.nanoTime();
        log.info("warehouse pay overview query started {}", StructuredArguments.entries(buildPayOverviewFields(
            "warehouse_pay_overview_query_started",
            request,
            null
        )));
        PayOverviewResponse response = dorisQueryService.queryPayOverview(request);
        log.info("warehouse pay overview query completed {}", StructuredArguments.entries(buildPayOverviewFields(
            "warehouse_pay_overview_query_completed",
            request,
            Map.of(
                "orderCount", response.getOrderCount(),
                "paidOrderCount", response.getPaidOrderCount(),
                "durationMs", (System.nanoTime() - startedAtNanos) / 1_000_000L
            )
        )));
        return ApiResponse.success(response);
    }

    @GetMapping("/pay/trend")
    public ApiResponse<List<PayTrendResponse>> queryPayTrend(@Valid @ModelAttribute SummaryQueryRequest request) {
        long startedAtNanos = System.nanoTime();
        log.info("warehouse pay trend query started {}", StructuredArguments.entries(buildSummaryQueryFields(
            "warehouse_pay_trend_query_started",
            request,
            null
        )));
        List<PayTrendResponse> response = dorisQueryService.queryPayTrend(request);
        log.info("warehouse pay trend query completed {}", StructuredArguments.entries(buildSummaryQueryFields(
            "warehouse_pay_trend_query_completed",
            request,
            Map.of(
                "resultSize", response.size(),
                "durationMs", (System.nanoTime() - startedAtNanos) / 1_000_000L
            )
        )));
        return ApiResponse.success(response);
    }

    @GetMapping("/jobs/logs")
    public ApiResponse<List<SyncJobLogResponse>> queryRecentJobLogs(
        @RequestParam(name = "jobCode", required = false) String jobCode,
        @RequestParam(name = "limit", defaultValue = "20") int limit
    ) {
        long startedAtNanos = System.nanoTime();
        int safeLimit = Math.max(1, Math.min(limit, 100));
        log.info("warehouse job logs query started {}", StructuredArguments.entries(buildJobLogsFields(
            "warehouse_job_logs_query_started",
            jobCode,
            safeLimit,
            null
        )));
        List<SyncJobLogResponse> response = dorisQueryService.queryRecentJobLogs(jobCode, safeLimit);
        log.info("warehouse job logs query completed {}", StructuredArguments.entries(buildJobLogsFields(
            "warehouse_job_logs_query_completed",
            jobCode,
            safeLimit,
            Map.of(
                "resultSize", response.size(),
                "durationMs", (System.nanoTime() - startedAtNanos) / 1_000_000L
            )
        )));
        return ApiResponse.success(response);
    }

    @GetMapping("/quality/results")
    public ApiResponse<List<QualityResultResponse>> queryQualityResults(
        @RequestParam(name = "limit", defaultValue = "20") int limit
    ) {
        long startedAtNanos = System.nanoTime();
        int safeLimit = Math.max(1, Math.min(limit, 100));
        log.info("warehouse quality results query started {}", StructuredArguments.entries(buildSimpleFields(
            "warehouse_quality_results_query_started",
            Map.of("limit", safeLimit)
        )));
        List<QualityResultResponse> response = dorisQueryService.queryQualityResults(safeLimit);
        log.info("warehouse quality results query completed {}", StructuredArguments.entries(buildSimpleFields(
            "warehouse_quality_results_query_completed",
            Map.of(
                "limit", safeLimit,
                "resultSize", response.size(),
                "durationMs", (System.nanoTime() - startedAtNanos) / 1_000_000L
            )
        )));
        return ApiResponse.success(response);
    }

    private Map<String, Object> buildOrderQueryFields(
        String event,
        OrderQueryRequest request,
        Map<String, Object> extraFields
    ) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("event", event);
        fields.put("pageNum", request.getPageNum());
        fields.put("pageSize", request.getPageSize());
        fields.put("startDate", request.getStartDate());
        fields.put("endDate", request.getEndDate());
        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            fields.put("keyword", request.getKeyword().trim());
        }
        mergeExtra(fields, extraFields);
        return fields;
    }

    private Map<String, Object> buildSummaryQueryFields(
        String event,
        SummaryQueryRequest request,
        Map<String, Object> extraFields
    ) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("event", event);
        fields.put("startDate", request.getStartDate());
        fields.put("endDate", request.getEndDate());
        mergeExtra(fields, extraFields);
        return fields;
    }

    private Map<String, Object> buildPayOverviewFields(
        String event,
        PayOverviewQueryRequest request,
        Map<String, Object> extraFields
    ) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("event", event);
        fields.put("startDate", request.getStartDate());
        fields.put("endDate", request.getEndDate());
        mergeExtra(fields, extraFields);
        return fields;
    }

    private Map<String, Object> buildSimpleFields(String event, Map<String, Object> extraFields) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("event", event);
        mergeExtra(fields, extraFields);
        return fields;
    }

    private Map<String, Object> buildJobLogsFields(
        String event,
        String jobCode,
        int limit,
        Map<String, Object> extraFields
    ) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("event", event);
        fields.put("limit", limit);
        if (jobCode != null && !jobCode.isBlank()) {
            fields.put("jobCode", jobCode.trim());
        }
        mergeExtra(fields, extraFields);
        return fields;
    }

    private void mergeExtra(Map<String, Object> target, Map<String, Object> extraFields) {
        if (extraFields == null || extraFields.isEmpty()) {
            return;
        }
        extraFields.forEach((key, value) -> {
            if (value != null) {
                target.put(key, value);
            }
        });
    }
}
