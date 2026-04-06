package com.bestwo.dataplatform.warehouse.service;

import com.bestwo.dataplatform.warehouse.dto.AlertRecordResponse;
import com.bestwo.dataplatform.warehouse.dto.LineageRelationResponse;
import com.bestwo.dataplatform.warehouse.mapper.WarehouseDorisMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WarehouseGovernanceService {

    private final WarehouseMetadataService warehouseMetadataService;
    private final WarehouseDorisMapper warehouseDorisMapper;

    public WarehouseGovernanceService(
        WarehouseMetadataService warehouseMetadataService,
        WarehouseDorisMapper warehouseDorisMapper
    ) {
        this.warehouseMetadataService = warehouseMetadataService;
        this.warehouseDorisMapper = warehouseDorisMapper;
    }

    public List<LineageRelationResponse> queryLineageRelations(String tableCode, int limit) {
        warehouseMetadataService.ensureGovernanceArtifacts();
        return warehouseDorisMapper.queryLineageRelations(tableCode, limit);
    }

    public List<AlertRecordResponse> queryAlertRecords(String alertStatus, int limit) {
        warehouseMetadataService.ensureGovernanceArtifacts();
        return warehouseDorisMapper.queryAlertRecords(alertStatus, limit);
    }
}
