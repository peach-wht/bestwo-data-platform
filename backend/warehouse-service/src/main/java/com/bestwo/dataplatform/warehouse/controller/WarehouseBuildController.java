package com.bestwo.dataplatform.warehouse.controller;

import com.bestwo.dataplatform.common.api.ApiResponse;
import com.bestwo.dataplatform.warehouse.dto.DwBuildRunResponse;
import com.bestwo.dataplatform.warehouse.dto.SyncJobLogResponse;
import com.bestwo.dataplatform.warehouse.service.DwdBuildService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WarehouseBuildController {

    private final DwdBuildService dwdBuildService;

    public WarehouseBuildController(DwdBuildService dwdBuildService) {
        this.dwdBuildService = dwdBuildService;
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
}
