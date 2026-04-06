package com.bestwo.dataplatform.warehouse.service;

import com.bestwo.dataplatform.common.exception.BusinessException;
import com.bestwo.dataplatform.warehouse.config.DorisProperties;
import com.bestwo.dataplatform.warehouse.config.SourcePostgresProperties;
import com.bestwo.dataplatform.warehouse.dto.JobDefinitionResponse;
import com.bestwo.dataplatform.warehouse.dto.JobExecutionLogResponse;
import com.bestwo.dataplatform.warehouse.dto.LineageRelationResponse;
import com.bestwo.dataplatform.warehouse.dto.MetadataColumnResponse;
import com.bestwo.dataplatform.warehouse.dto.MetadataDatasourceResponse;
import com.bestwo.dataplatform.warehouse.dto.MetadataInitResponse;
import com.bestwo.dataplatform.warehouse.dto.MetadataTableColumnSnapshot;
import com.bestwo.dataplatform.warehouse.dto.MetadataTableResponse;
import com.bestwo.dataplatform.warehouse.entity.DwLineageRelationEntity;
import com.bestwo.dataplatform.warehouse.entity.DwMetaColumnEntity;
import com.bestwo.dataplatform.warehouse.entity.DwMetaDatasourceEntity;
import com.bestwo.dataplatform.warehouse.entity.DwMetaTableEntity;
import com.bestwo.dataplatform.warehouse.entity.DwSyncJobEntity;
import com.bestwo.dataplatform.warehouse.mapper.WarehouseDorisMapper;
import com.bestwo.dataplatform.warehouse.source.mapper.SourceMetadataMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class WarehouseMetadataService {

    private static final String SOURCE_DATASOURCE_CODE = "postgres-bestwo-app";
    private static final String TARGET_DATASOURCE_CODE = "doris-bestwo-dw";
    private static final List<String> META_SCHEMA_RESOURCES = List.of(
        "sql/doris/meta/01_create_dw_sync_job.sql",
        "sql/doris/meta/02_create_dw_sync_job_log.sql",
        "sql/doris/meta/03_create_dw_meta_datasource.sql",
        "sql/doris/meta/04_create_dw_meta_table.sql",
        "sql/doris/meta/05_create_dw_meta_column.sql",
        "sql/doris/meta/06_create_dw_job_log.sql",
        "sql/doris/meta/07_create_dw_quality_rule.sql",
        "sql/doris/meta/08_create_dw_quality_result.sql",
        "sql/doris/meta/09_create_dw_lineage_relation.sql",
        "sql/doris/meta/10_create_dw_alert_record.sql"
    );
    private static final List<String> TARGET_SCHEMA_RESOURCES = List.of(
        "sql/doris/dwd/01_create_dwd_wx_order_detail.sql",
        "sql/doris/dws/01_create_dws_wx_pay_trade_day.sql",
        "sql/doris/ads/01_create_ads_order_day_summary.sql",
        "sql/doris/ads/02_create_ads_pay_dashboard_overview.sql"
    );

    private final WarehouseDorisMapper warehouseDorisMapper;
    private final SourceMetadataMapper sourceMetadataMapper;
    private final DorisSchemaService dorisSchemaService;
    private final DorisProperties dorisProperties;
    private final SourcePostgresProperties sourcePostgresProperties;

    public WarehouseMetadataService(
        WarehouseDorisMapper warehouseDorisMapper,
        SourceMetadataMapper sourceMetadataMapper,
        DorisSchemaService dorisSchemaService,
        DorisProperties dorisProperties,
        SourcePostgresProperties sourcePostgresProperties
    ) {
        this.warehouseDorisMapper = warehouseDorisMapper;
        this.sourceMetadataMapper = sourceMetadataMapper;
        this.dorisSchemaService = dorisSchemaService;
        this.dorisProperties = dorisProperties;
        this.sourcePostgresProperties = sourcePostgresProperties;
    }

    public MetadataInitResponse initMetadata() {
        dorisSchemaService.initOdsSchema();

        List<String> executedResources = new ArrayList<>();
        for (String resourcePath : META_SCHEMA_RESOURCES) {
            executeSqlResource(resourcePath);
            executedResources.add(resourcePath);
        }
        for (String resourcePath : TARGET_SCHEMA_RESOURCES) {
            executeSqlResource(resourcePath);
            executedResources.add(resourcePath);
        }

        registerDatasources();
        registerKnownJobs();
        registerLineageRelations();

        int tableCount = registerTablesAndColumns();

        MetadataInitResponse response = new MetadataInitResponse();
        response.setDatabase(dorisProperties.getDatabase());
        response.setDatasourceCount(2);
        response.setTableCount(tableCount);
        response.setColumnCount(countRegisteredColumns());
        response.setExecutedResources(executedResources);
        return response;
    }

    public List<MetadataDatasourceResponse> queryDatasources() {
        ensureGovernanceTables();
        return warehouseDorisMapper.queryMetaDatasources();
    }

    public List<MetadataTableResponse> queryTables(String datasourceCode, String tableLayer, int limit) {
        ensureGovernanceTables();
        return warehouseDorisMapper.queryMetaTables(datasourceCode, tableLayer, limit);
    }

    public List<MetadataColumnResponse> queryColumns(String tableCode, int limit) {
        ensureGovernanceTables();
        return warehouseDorisMapper.queryMetaColumns(tableCode, limit);
    }

    public List<JobDefinitionResponse> queryJobs(int limit) {
        ensureGovernanceTables();
        return warehouseDorisMapper.queryJobDefinitions(limit);
    }

    public List<JobExecutionLogResponse> queryJobLogs(String jobCode, int limit) {
        ensureGovernanceArtifacts();
        if (warehouseDorisMapper.listTableColumns(dorisProperties.getDatabase(), "dw_job_log").isEmpty()) {
            return List.of();
        }
        return warehouseDorisMapper.queryDwJobLogs(jobCode, limit);
    }

    public void ensureGovernanceArtifacts() {
        ensureGovernanceTables();
        registerLineageRelations();
    }

    private void registerDatasources() {
        Instant now = Instant.now();
        warehouseDorisMapper.saveMetaDatasource(buildSourceDatasource(now));
        warehouseDorisMapper.saveMetaDatasource(buildTargetDatasource(now));
    }

    private void registerKnownJobs() {
        Instant now = Instant.now();
        Set<String> existingJobCodes = new HashSet<>();
        for (JobDefinitionResponse job : warehouseDorisMapper.queryJobDefinitions(100)) {
            existingJobCodes.add(job.getJobCode());
        }

        saveJobIfAbsent(existingJobCodes, buildJobDefinition(
            "sync-order-to-ods",
            "Sync order-service business tables to Doris ODS",
            "POSTGRESQL",
            "biz_order,biz_payment_order,biz_payment_notify_log",
            "ods_wx_order,ods_wx_payment_order,ods_wx_payment_notify",
            "Manual MVP sync job from order-service PostgreSQL to Doris ODS",
            now
        ));
        saveJobIfAbsent(existingJobCodes, buildJobDefinition(
            "build-dwd-order-detail",
            "Build DWD order detail from Doris ODS",
            "DORIS",
            "ods_wx_order,ods_wx_payment_order,ods_wx_payment_notify",
            "dwd_wx_order_detail",
            "Manual MVP DWD build job from ODS to dwd_wx_order_detail",
            now
        ));
        saveJobIfAbsent(existingJobCodes, buildJobDefinition(
            "build-ads-order-metrics",
            "Build DWS and ADS order metrics from DWD",
            "DORIS",
            "dwd_wx_order_detail",
            "dws_wx_pay_trade_day,ads_order_day_summary,ads_pay_dashboard_overview",
            "Manual MVP ADS build job from DWD to DWS/ADS",
            now
        ));
        saveJobIfAbsent(existingJobCodes, buildJobDefinition(
            "run-quality-check",
            "Run warehouse quality checks",
            "DORIS",
            "ods_wx_order,dwd_wx_order_detail,ods_wx_payment_notify,ads_order_day_summary",
            "dw_quality_result",
            "Manual quality check job for MVP warehouse governance",
            now
        ));
    }

    private int registerTablesAndColumns() {
        Map<String, List<MetadataTableColumnSnapshot>> sourceColumns = sourceMetadataMapper.listTableColumnDetails(
            List.of("biz_order", "biz_payment_order", "biz_payment_notify_log")
        ).stream().collect(Collectors.groupingBy(MetadataTableColumnSnapshot::getTableName, LinkedHashMap::new, Collectors.toList()));

        List<TableRegistrationSpec> specs = List.of(
            new TableRegistrationSpec(SOURCE_DATASOURCE_CODE, "biz_order", "BIZ", "SOURCE", "Business order table", "order-service", "MANUAL_WRITE"),
            new TableRegistrationSpec(SOURCE_DATASOURCE_CODE, "biz_payment_order", "BIZ", "SOURCE", "Business payment order table", "order-service", "MANUAL_WRITE"),
            new TableRegistrationSpec(SOURCE_DATASOURCE_CODE, "biz_payment_notify_log", "BIZ", "SOURCE", "Business payment notify log table", "order-service", "MANUAL_WRITE"),
            new TableRegistrationSpec(TARGET_DATASOURCE_CODE, "ods_wx_order", "ODS", "WAREHOUSE", "ODS order snapshot table", "warehouse-service", "MANUAL_SYNC"),
            new TableRegistrationSpec(TARGET_DATASOURCE_CODE, "ods_wx_payment_order", "ODS", "WAREHOUSE", "ODS payment order snapshot table", "warehouse-service", "MANUAL_SYNC"),
            new TableRegistrationSpec(TARGET_DATASOURCE_CODE, "ods_wx_payment_notify", "ODS", "WAREHOUSE", "ODS payment notify snapshot table", "warehouse-service", "MANUAL_SYNC"),
            new TableRegistrationSpec(TARGET_DATASOURCE_CODE, "dwd_wx_order_detail", "DWD", "WAREHOUSE", "Unified order detail wide table", "warehouse-service", "MANUAL_BUILD"),
            new TableRegistrationSpec(TARGET_DATASOURCE_CODE, "dws_wx_pay_trade_day", "DWS", "WAREHOUSE", "Daily payment trade aggregate table", "warehouse-service", "MANUAL_BUILD"),
            new TableRegistrationSpec(TARGET_DATASOURCE_CODE, "ads_order_day_summary", "ADS", "WAREHOUSE", "Order day summary table", "warehouse-service", "MANUAL_BUILD"),
            new TableRegistrationSpec(TARGET_DATASOURCE_CODE, "ads_pay_dashboard_overview", "ADS", "WAREHOUSE", "Payment dashboard overview table", "warehouse-service", "MANUAL_BUILD"),
            new TableRegistrationSpec(TARGET_DATASOURCE_CODE, "dw_sync_job", "META", "SYSTEM", "Sync job definition table", "warehouse-service", "SYSTEM"),
            new TableRegistrationSpec(TARGET_DATASOURCE_CODE, "dw_sync_job_log", "META", "SYSTEM", "Legacy sync job log table", "warehouse-service", "SYSTEM"),
            new TableRegistrationSpec(TARGET_DATASOURCE_CODE, "dw_job_log", "META", "SYSTEM", "Unified job log table", "warehouse-service", "SYSTEM"),
            new TableRegistrationSpec(TARGET_DATASOURCE_CODE, "dw_quality_rule", "META", "SYSTEM", "Quality rule definition table", "warehouse-service", "SYSTEM"),
            new TableRegistrationSpec(TARGET_DATASOURCE_CODE, "dw_quality_result", "META", "SYSTEM", "Quality execution result table", "warehouse-service", "SYSTEM"),
            new TableRegistrationSpec(TARGET_DATASOURCE_CODE, "dw_lineage_relation", "META", "SYSTEM", "Table level lineage relation table", "warehouse-service", "SYSTEM"),
            new TableRegistrationSpec(TARGET_DATASOURCE_CODE, "dw_alert_record", "META", "SYSTEM", "Warehouse governance alert record table", "warehouse-service", "SYSTEM"),
            new TableRegistrationSpec(TARGET_DATASOURCE_CODE, "dw_meta_datasource", "META", "SYSTEM", "Datasource metadata table", "warehouse-service", "SYSTEM"),
            new TableRegistrationSpec(TARGET_DATASOURCE_CODE, "dw_meta_table", "META", "SYSTEM", "Table metadata table", "warehouse-service", "SYSTEM"),
            new TableRegistrationSpec(TARGET_DATASOURCE_CODE, "dw_meta_column", "META", "SYSTEM", "Column metadata table", "warehouse-service", "SYSTEM")
        );

        Instant now = Instant.now();
        Timestamp nowTs = Timestamp.from(now);
        int tableCount = 0;

        for (TableRegistrationSpec spec : specs) {
            List<MetadataTableColumnSnapshot> columns = SOURCE_DATASOURCE_CODE.equals(spec.datasourceCode())
                ? sourceColumns.getOrDefault(spec.tableName(), List.of())
                : warehouseDorisMapper.listTableColumnDetails(dorisProperties.getDatabase(), spec.tableName());

            warehouseDorisMapper.saveMetaTable(buildMetaTable(spec, columns.size(), nowTs));
            if (!columns.isEmpty()) {
                warehouseDorisMapper.saveMetaColumns(buildMetaColumns(spec.tableName(), columns, nowTs));
            }
            tableCount++;
        }

        return tableCount;
    }

    private int countRegisteredColumns() {
        long count = 0L;
        for (MetadataTableResponse table : warehouseDorisMapper.queryMetaTables(null, null, 200)) {
            if (table.getColumnCount() != null) {
                count += table.getColumnCount();
            }
        }
        return (int) count;
    }

    private DwMetaDatasourceEntity buildSourceDatasource(Instant now) {
        DwMetaDatasourceEntity entity = new DwMetaDatasourceEntity();
        entity.setDatasourceCode(SOURCE_DATASOURCE_CODE);
        entity.setDatasourceName("order-service PostgreSQL");
        entity.setDatasourceType("POSTGRESQL");
        entity.setHost(sourcePostgresProperties.getHost());
        entity.setPort(sourcePostgresProperties.getPort());
        entity.setDatabaseName(sourcePostgresProperties.getDatabase());
        entity.setOwner("order-service");
        entity.setDescription("Primary OLTP datasource for order and payment business tables");
        entity.setEnabled(1);
        entity.setCreatedAt(Timestamp.from(now));
        entity.setUpdatedAt(Timestamp.from(now));
        return entity;
    }

    private DwMetaDatasourceEntity buildTargetDatasource(Instant now) {
        DwMetaDatasourceEntity entity = new DwMetaDatasourceEntity();
        entity.setDatasourceCode(TARGET_DATASOURCE_CODE);
        entity.setDatasourceName("warehouse Doris");
        entity.setDatasourceType("DORIS");
        entity.setHost(dorisProperties.getHost());
        entity.setPort(dorisProperties.getPort());
        entity.setDatabaseName(dorisProperties.getDatabase());
        entity.setOwner("warehouse-service");
        entity.setDescription("Primary warehouse datasource for ODS, DWD, DWS, ADS and governance tables");
        entity.setEnabled(1);
        entity.setCreatedAt(Timestamp.from(now));
        entity.setUpdatedAt(Timestamp.from(now));
        return entity;
    }

    private DwSyncJobEntity buildJobDefinition(
        String jobCode,
        String jobName,
        String sourceType,
        String sourceTables,
        String targetTables,
        String remark,
        Instant now
    ) {
        DwSyncJobEntity job = new DwSyncJobEntity();
        job.setJobCode(jobCode);
        job.setJobName(jobName);
        job.setSourceType(sourceType);
        job.setSourceTables(sourceTables);
        job.setTargetTables(targetTables);
        job.setEnabled(1);
        job.setRemark(remark);
        job.setLastRunStatus("NOT_RUN");
        job.setLastRunMessage("metadata initialized");
        job.setLastRunAt(Timestamp.from(now));
        job.setCreatedAt(Timestamp.from(now));
        job.setUpdatedAt(Timestamp.from(now));
        return job;
    }

    private void saveJobIfAbsent(Set<String> existingJobCodes, DwSyncJobEntity job) {
        if (existingJobCodes.contains(job.getJobCode())) {
            return;
        }
        warehouseDorisMapper.saveSyncJob(job);
    }

    private void registerLineageRelations() {
        List<LineageRelationResponse> existingRelations = warehouseDorisMapper.queryLineageRelations(null, 1);
        if (!existingRelations.isEmpty()) {
            return;
        }

        Timestamp now = Timestamp.from(Instant.now());
        warehouseDorisMapper.saveLineageRelations(List.of(
            buildLineageRelation("LN001", SOURCE_DATASOURCE_CODE, "biz_order", TARGET_DATASOURCE_CODE, "ods_wx_order", "sync-order-to-ods", now),
            buildLineageRelation("LN002", SOURCE_DATASOURCE_CODE, "biz_payment_order", TARGET_DATASOURCE_CODE, "ods_wx_payment_order", "sync-order-to-ods", now),
            buildLineageRelation("LN003", SOURCE_DATASOURCE_CODE, "biz_payment_notify_log", TARGET_DATASOURCE_CODE, "ods_wx_payment_notify", "sync-order-to-ods", now),
            buildLineageRelation("LN004", TARGET_DATASOURCE_CODE, "ods_wx_order", TARGET_DATASOURCE_CODE, "dwd_wx_order_detail", "build-dwd-order-detail", now),
            buildLineageRelation("LN005", TARGET_DATASOURCE_CODE, "ods_wx_payment_order", TARGET_DATASOURCE_CODE, "dwd_wx_order_detail", "build-dwd-order-detail", now),
            buildLineageRelation("LN006", TARGET_DATASOURCE_CODE, "ods_wx_payment_notify", TARGET_DATASOURCE_CODE, "dwd_wx_order_detail", "build-dwd-order-detail", now),
            buildLineageRelation("LN007", TARGET_DATASOURCE_CODE, "dwd_wx_order_detail", TARGET_DATASOURCE_CODE, "dws_wx_pay_trade_day", "build-ads-order-metrics", now),
            buildLineageRelation("LN008", TARGET_DATASOURCE_CODE, "dws_wx_pay_trade_day", TARGET_DATASOURCE_CODE, "ads_order_day_summary", "build-ads-order-metrics", now),
            buildLineageRelation("LN009", TARGET_DATASOURCE_CODE, "dws_wx_pay_trade_day", TARGET_DATASOURCE_CODE, "ads_pay_dashboard_overview", "build-ads-order-metrics", now)
        ));
    }

    private DwMetaTableEntity buildMetaTable(TableRegistrationSpec spec, int columnCount, Timestamp now) {
        DwMetaTableEntity entity = new DwMetaTableEntity();
        entity.setTableCode(spec.tableName());
        entity.setDatasourceCode(spec.datasourceCode());
        entity.setTableName(spec.tableName());
        entity.setTableLayer(spec.tableLayer());
        entity.setTableCategory(spec.tableCategory());
        entity.setTableComment(spec.tableComment());
        entity.setOwner(spec.owner());
        entity.setRefreshStrategy(spec.refreshStrategy());
        entity.setColumnCount(columnCount);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return entity;
    }

    private List<DwMetaColumnEntity> buildMetaColumns(
        String tableCode,
        List<MetadataTableColumnSnapshot> columns,
        Timestamp now
    ) {
        List<DwMetaColumnEntity> entities = new ArrayList<>();
        for (MetadataTableColumnSnapshot column : columns) {
            DwMetaColumnEntity entity = new DwMetaColumnEntity();
            entity.setTableCode(tableCode);
            entity.setColumnName(column.getColumnName());
            entity.setDataType(column.getDataType());
            entity.setNullableFlag("YES".equalsIgnoreCase(column.getIsNullable()) ? 1 : 0);
            entity.setColumnComment(column.getColumnComment());
            entity.setColumnOrder(column.getOrdinalPosition());
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            entities.add(entity);
        }
        return entities;
    }

    private DwLineageRelationEntity buildLineageRelation(
        String relationId,
        String upstreamDatasourceCode,
        String upstreamTableCode,
        String downstreamDatasourceCode,
        String downstreamTableCode,
        String transformName,
        Timestamp now
    ) {
        DwLineageRelationEntity entity = new DwLineageRelationEntity();
        entity.setRelationId(relationId);
        entity.setRelationType("TABLE_TO_TABLE");
        entity.setUpstreamDatasourceCode(upstreamDatasourceCode);
        entity.setUpstreamTableCode(upstreamTableCode);
        entity.setDownstreamDatasourceCode(downstreamDatasourceCode);
        entity.setDownstreamTableCode(downstreamTableCode);
        entity.setTransformName(transformName);
        entity.setEnabled(1);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return entity;
    }

    private void ensureGovernanceTables() {
        for (String resourcePath : META_SCHEMA_RESOURCES) {
            executeSqlResource(resourcePath);
        }
    }

    private void executeSqlResource(String resourcePath) {
        String script = readClasspathResource(resourcePath);
        for (String statement : splitStatements(script)) {
            warehouseDorisMapper.executeSql(statement);
        }
    }

    private String readClasspathResource(String resourcePath) {
        try {
            return new String(new ClassPathResource(resourcePath).getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new BusinessException("failed to read resource: " + resourcePath);
        }
    }

    private List<String> splitStatements(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (String line : script.split("\\r?\\n")) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("--")) {
                continue;
            }
            builder.append(line).append('\n');
            if (trimmedLine.endsWith(";")) {
                String statement = builder.toString().trim();
                if (statement.endsWith(";")) {
                    statement = statement.substring(0, statement.length() - 1).trim();
                }
                if (!statement.isEmpty()) {
                    statements.add(statement);
                }
                builder.setLength(0);
            }
        }
        String tail = builder.toString().trim();
        if (!tail.isEmpty()) {
            statements.add(tail);
        }
        return statements;
    }

    private record TableRegistrationSpec(
        String datasourceCode,
        String tableName,
        String tableLayer,
        String tableCategory,
        String tableComment,
        String owner,
        String refreshStrategy
    ) {
    }
}
