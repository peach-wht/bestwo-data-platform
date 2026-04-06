package com.bestwo.dataplatform.warehouse.controller;

import com.bestwo.dataplatform.common.api.ApiResponse;
import com.bestwo.dataplatform.warehouse.dto.JobExecutionLogResponse;
import com.bestwo.dataplatform.warehouse.dto.QualityRuleResponse;
import com.bestwo.dataplatform.warehouse.dto.QualityRunResponse;
import com.bestwo.dataplatform.warehouse.service.QualityCheckService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WarehouseQualityController {

    private final QualityCheckService qualityCheckService;

    public WarehouseQualityController(QualityCheckService qualityCheckService) {
        this.qualityCheckService = qualityCheckService;
    }

    @GetMapping("/warehouse/quality/rules")
    public ApiResponse<List<QualityRuleResponse>> queryRules(
        @RequestParam(name = "limit", defaultValue = "20") int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return ApiResponse.success(qualityCheckService.queryRules(safeLimit));
    }

    @PostMapping({"/warehouse/quality/run", "/warehouse/jobs/quality/run", "/dw/jobs/quality/run"})
    public ApiResponse<QualityRunResponse> runQualityChecks() {
        return ApiResponse.success(qualityCheckService.runQualityChecks());
    }

    @GetMapping({"/warehouse/quality/logs", "/warehouse/jobs/quality/logs", "/dw/jobs/quality/logs"})
    public ApiResponse<List<JobExecutionLogResponse>> queryLogs(
        @RequestParam(name = "limit", defaultValue = "20") int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return ApiResponse.success(qualityCheckService.queryLatestLogs(safeLimit));
    }
}
