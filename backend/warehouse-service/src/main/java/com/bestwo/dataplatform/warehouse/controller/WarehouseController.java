package com.bestwo.dataplatform.warehouse.controller;

import com.bestwo.dataplatform.common.api.ApiResponse;
import com.bestwo.dataplatform.warehouse.service.DorisQueryService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
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
        try {
            return ApiResponse.success(dorisQueryService.ping());
        } catch (Exception ex) {
            return ApiResponse.fail("doris ping failed: " + ex.getMessage());
        }
    }

    @GetMapping("/orders/test")
    public ApiResponse<List<Map<String, Object>>> testOrders() {
        try {
            return ApiResponse.success(dorisQueryService.queryTestOrders());
        } catch (Exception ex) {
            return ApiResponse.fail("query test orders failed: " + ex.getMessage());
        }
    }
}
