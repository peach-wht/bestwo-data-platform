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
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/warehouse")
public class WarehouseController {

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
        OrderPageResponse response = new OrderPageResponse();
        response.setList(dorisQueryService.queryOrders(request));
        response.setPageNum(request.getPageNum());
        response.setPageSize(request.getPageSize());
        response.setTotal(dorisQueryService.countOrders(request));
        return ApiResponse.success(response);
    }

    @GetMapping("/summary/day")
    public ApiResponse<List<OrderSummaryDayResponse>> queryDaySummary(
        @Valid @ModelAttribute SummaryQueryRequest request
    ) {
        return ApiResponse.success(dorisQueryService.queryDaySummary(request));
    }

    @GetMapping("/pay/overview")
    public ApiResponse<PayOverviewResponse> queryPayOverview(@Valid @ModelAttribute PayOverviewQueryRequest request) {
        return ApiResponse.success(dorisQueryService.queryPayOverview(request));
    }

    @GetMapping("/pay/trend")
    public ApiResponse<List<PayTrendResponse>> queryPayTrend(@Valid @ModelAttribute SummaryQueryRequest request) {
        return ApiResponse.success(dorisQueryService.queryPayTrend(request));
    }

    @GetMapping("/jobs/logs")
    public ApiResponse<List<SyncJobLogResponse>> queryRecentJobLogs(
        @RequestParam(name = "jobCode", required = false) String jobCode,
        @RequestParam(name = "limit", defaultValue = "20") int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return ApiResponse.success(dorisQueryService.queryRecentJobLogs(jobCode, safeLimit));
    }

    @GetMapping("/quality/results")
    public ApiResponse<List<QualityResultResponse>> queryQualityResults(
        @RequestParam(name = "limit", defaultValue = "20") int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return ApiResponse.success(dorisQueryService.queryQualityResults(safeLimit));
    }
}
