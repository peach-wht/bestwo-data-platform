package com.bestwo.dataplatform.warehouse.controller;

import com.bestwo.dataplatform.common.api.ApiResponse;
import com.bestwo.dataplatform.warehouse.dto.JobDefinitionResponse;
import com.bestwo.dataplatform.warehouse.dto.JobExecutionLogResponse;
import com.bestwo.dataplatform.warehouse.dto.MetadataColumnResponse;
import com.bestwo.dataplatform.warehouse.dto.MetadataDatasourceResponse;
import com.bestwo.dataplatform.warehouse.dto.MetadataInitResponse;
import com.bestwo.dataplatform.warehouse.dto.MetadataTableResponse;
import com.bestwo.dataplatform.warehouse.service.WarehouseMetadataService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/warehouse/meta")
public class WarehouseMetadataController {

    private final WarehouseMetadataService warehouseMetadataService;

    public WarehouseMetadataController(WarehouseMetadataService warehouseMetadataService) {
        this.warehouseMetadataService = warehouseMetadataService;
    }

    @PostMapping("/init")
    public ApiResponse<MetadataInitResponse> initMetadata() {
        return ApiResponse.success(warehouseMetadataService.initMetadata());
    }

    @GetMapping("/datasources")
    public ApiResponse<List<MetadataDatasourceResponse>> queryDatasources() {
        return ApiResponse.success(warehouseMetadataService.queryDatasources());
    }

    @GetMapping("/tables")
    public ApiResponse<List<MetadataTableResponse>> queryTables(
        @RequestParam(name = "datasourceCode", required = false) String datasourceCode,
        @RequestParam(name = "tableLayer", required = false) String tableLayer,
        @RequestParam(name = "limit", defaultValue = "100") int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        return ApiResponse.success(warehouseMetadataService.queryTables(datasourceCode, tableLayer, safeLimit));
    }

    @GetMapping("/columns")
    public ApiResponse<List<MetadataColumnResponse>> queryColumns(
        @RequestParam(name = "tableCode") String tableCode,
        @RequestParam(name = "limit", defaultValue = "200") int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 500));
        return ApiResponse.success(warehouseMetadataService.queryColumns(tableCode, safeLimit));
    }

    @GetMapping("/jobs")
    public ApiResponse<List<JobDefinitionResponse>> queryJobs(
        @RequestParam(name = "limit", defaultValue = "50") int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        return ApiResponse.success(warehouseMetadataService.queryJobs(safeLimit));
    }

    @GetMapping("/job-logs")
    public ApiResponse<List<JobExecutionLogResponse>> queryJobLogs(
        @RequestParam(name = "jobCode", required = false) String jobCode,
        @RequestParam(name = "limit", defaultValue = "20") int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        return ApiResponse.success(warehouseMetadataService.queryJobLogs(jobCode, safeLimit));
    }
}
