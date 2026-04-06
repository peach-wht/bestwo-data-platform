package com.bestwo.dataplatform.warehouse.controller;

import com.bestwo.dataplatform.common.api.ApiResponse;
import com.bestwo.dataplatform.warehouse.dto.AdsBuildRunResponse;
import com.bestwo.dataplatform.warehouse.dto.DwBuildRunResponse;
import com.bestwo.dataplatform.warehouse.dto.SyncJobLogResponse;
import com.bestwo.dataplatform.warehouse.service.AdsBuildService;
import com.bestwo.dataplatform.warehouse.service.DwdBuildService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WarehouseBuildController {

    private final DwdBuildService dwdBuildService;
    private final AdsBuildService adsBuildService;

    public WarehouseBuildController(DwdBuildService dwdBuildService, AdsBuildService adsBuildService) {
        this.dwdBuildService = dwdBuildService;
        this.adsBuildService = adsBuildService;
    }

    @PostMapping({"/dw/jobs/build-dwd/run", "/warehouse/jobs/build-dwd/run"})
    public ApiResponse<DwBuildRunResponse> runBuildDwdJob() {
        return ApiResponse.success(dwdBuildService.runBuild());
    }

    @GetMapping({"/dw/jobs/build-dwd/logs", "/warehouse/jobs/build-dwd/logs"})
    public ApiResponse<List<SyncJobLogResponse>> queryBuildDwdLogs(
        @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        return ApiResponse.success(dwdBuildService.queryLatestLogs(safeLimit));
    }

    @PostMapping({"/dw/jobs/build-ads/run", "/warehouse/jobs/build-ads/run"})
    public ApiResponse<AdsBuildRunResponse> runBuildAdsJob() {
        return ApiResponse.success(adsBuildService.runBuild());
    }

    @GetMapping({"/dw/jobs/build-ads/logs", "/warehouse/jobs/build-ads/logs"})
    public ApiResponse<List<SyncJobLogResponse>> queryBuildAdsLogs(
        @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        return ApiResponse.success(adsBuildService.queryLatestLogs(safeLimit));
    }
}
