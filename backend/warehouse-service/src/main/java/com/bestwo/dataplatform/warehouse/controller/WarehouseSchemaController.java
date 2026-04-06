package com.bestwo.dataplatform.warehouse.controller;

import com.bestwo.dataplatform.common.api.ApiResponse;
import com.bestwo.dataplatform.warehouse.service.DorisSchemaService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/warehouse/schema")
public class WarehouseSchemaController {

    private final DorisSchemaService dorisSchemaService;

    public WarehouseSchemaController(DorisSchemaService dorisSchemaService) {
        this.dorisSchemaService = dorisSchemaService;
    }

    @PostMapping("/ods/init")
    public ApiResponse<Map<String, Object>> initOdsSchema() {
        return ApiResponse.success(dorisSchemaService.initOdsSchema());
    }

    @GetMapping("/ods")
    public ApiResponse<Map<String, Object>> inspectOdsSchema() {
        return ApiResponse.success(dorisSchemaService.inspectOdsTables());
    }
}
