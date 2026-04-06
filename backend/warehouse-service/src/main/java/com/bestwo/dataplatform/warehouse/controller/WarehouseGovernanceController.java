package com.bestwo.dataplatform.warehouse.controller;

import com.bestwo.dataplatform.common.api.ApiResponse;
import com.bestwo.dataplatform.warehouse.dto.AlertRecordResponse;
import com.bestwo.dataplatform.warehouse.dto.LineageRelationResponse;
import com.bestwo.dataplatform.warehouse.service.WarehouseGovernanceService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/warehouse")
public class WarehouseGovernanceController {

    private final WarehouseGovernanceService warehouseGovernanceService;

    public WarehouseGovernanceController(WarehouseGovernanceService warehouseGovernanceService) {
        this.warehouseGovernanceService = warehouseGovernanceService;
    }

    @GetMapping("/lineage")
    public ApiResponse<List<LineageRelationResponse>> queryLineageRelations(
        @RequestParam(name = "tableCode", required = false) String tableCode,
        @RequestParam(name = "limit", defaultValue = "50") int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        return ApiResponse.success(warehouseGovernanceService.queryLineageRelations(tableCode, safeLimit));
    }

    @GetMapping("/alerts")
    public ApiResponse<List<AlertRecordResponse>> queryAlertRecords(
        @RequestParam(name = "alertStatus", required = false) String alertStatus,
        @RequestParam(name = "limit", defaultValue = "20") int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        return ApiResponse.success(warehouseGovernanceService.queryAlertRecords(alertStatus, safeLimit));
    }
}
