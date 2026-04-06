package com.bestwo.dataplatform.warehouse.controller;

import com.bestwo.dataplatform.common.api.ApiResponse;
import com.bestwo.dataplatform.warehouse.dto.SyncJobLogResponse;
import com.bestwo.dataplatform.warehouse.dto.SyncJobRunResponse;
import com.bestwo.dataplatform.warehouse.service.OrderSyncService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WarehouseSyncController {

    private final OrderSyncService orderSyncService;

    public WarehouseSyncController(OrderSyncService orderSyncService) {
        this.orderSyncService = orderSyncService;
    }

    @PostMapping({"/dw/jobs/sync-order/run", "/warehouse/jobs/sync-order/run"})
    public ApiResponse<SyncJobRunResponse> runSyncOrderJob() {
        return ApiResponse.success(orderSyncService.runSync());
    }

    @GetMapping({"/dw/jobs/sync-order/logs", "/warehouse/jobs/sync-order/logs"})
    public ApiResponse<List<SyncJobLogResponse>> querySyncOrderLogs(
        @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        return ApiResponse.success(orderSyncService.queryLatestLogs(safeLimit));
    }
}
