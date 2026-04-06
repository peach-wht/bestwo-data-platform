package com.bestwo.dataplatform.warehouse.service;

import com.bestwo.dataplatform.common.exception.BusinessException;
import com.bestwo.dataplatform.warehouse.dto.SyncJobLogResponse;
import com.bestwo.dataplatform.warehouse.dto.SyncJobRunResponse;
import com.bestwo.dataplatform.warehouse.entity.DwSyncJobEntity;
import com.bestwo.dataplatform.warehouse.entity.DwSyncJobLogEntity;
import com.bestwo.dataplatform.warehouse.mapper.WarehouseDorisMapper;
import com.bestwo.dataplatform.warehouse.source.mapper.SourceBizOrderMapper;
import com.bestwo.dataplatform.warehouse.source.mapper.SourceBizPaymentNotifyLogMapper;
import com.bestwo.dataplatform.warehouse.source.mapper.SourceBizPaymentOrderMapper;
import com.bestwo.dataplatform.warehouse.source.model.BizOrderSourceRow;
import com.bestwo.dataplatform.warehouse.source.model.BizPaymentNotifyLogSourceRow;
import com.bestwo.dataplatform.warehouse.source.model.BizPaymentOrderSourceRow;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class OrderSyncService {

    private static final String JOB_CODE = "sync-order-to-ods";
    private static final String JOB_NAME = "Sync order-service business tables to Doris ODS";
    private static final int BATCH_SIZE = 100;
    private static final List<String> META_SCHEMA_RESOURCES = List.of(
        "sql/doris/meta/01_create_dw_sync_job.sql",
        "sql/doris/meta/02_create_dw_sync_job_log.sql"
    );

    private final WarehouseDorisMapper warehouseDorisMapper;
    private final SourceBizOrderMapper sourceBizOrderMapper;
    private final SourceBizPaymentOrderMapper sourceBizPaymentOrderMapper;
    private final SourceBizPaymentNotifyLogMapper sourceBizPaymentNotifyLogMapper;
    private final DorisSchemaService dorisSchemaService;

    public OrderSyncService(
        WarehouseDorisMapper warehouseDorisMapper,
        SourceBizOrderMapper sourceBizOrderMapper,
        SourceBizPaymentOrderMapper sourceBizPaymentOrderMapper,
        SourceBizPaymentNotifyLogMapper sourceBizPaymentNotifyLogMapper,
        DorisSchemaService dorisSchemaService
    ) {
        this.warehouseDorisMapper = warehouseDorisMapper;
        this.sourceBizOrderMapper = sourceBizOrderMapper;
        this.sourceBizPaymentOrderMapper = sourceBizPaymentOrderMapper;
        this.sourceBizPaymentNotifyLogMapper = sourceBizPaymentNotifyLogMapper;
        this.dorisSchemaService = dorisSchemaService;
    }

    public SyncJobRunResponse runSync() {
        dorisSchemaService.initOdsSchema();
        ensureMetaTables();

        Instant startedAt = Instant.now();
        String logId = "SYNC-" + System.currentTimeMillis();

        long syncedOrderCount = 0L;
        long syncedPaymentOrderCount = 0L;
        long syncedNotifyLogCount = 0L;
        String runStatus = "SUCCESS";
        String message = "sync completed";

        try {
            syncedOrderCount = syncOrders();
            syncedPaymentOrderCount = syncPaymentOrders();
            syncedNotifyLogCount = syncPaymentNotifyLogs();
        } catch (RuntimeException exception) {
            runStatus = "FAILED";
            message = exception.getMessage() == null ? "sync failed" : exception.getMessage();
        }

        Instant finishedAt = Instant.now();
        long durationMs = finishedAt.toEpochMilli() - startedAt.toEpochMilli();

        saveSyncJob(buildSyncJob(runStatus, message, finishedAt));
        saveSyncJobLog(buildSyncJobLog(
            logId,
            runStatus,
            message,
            syncedOrderCount,
            syncedPaymentOrderCount,
            syncedNotifyLogCount,
            startedAt,
            finishedAt,
            durationMs
        ));

        if (!"SUCCESS".equals(runStatus)) {
            throw new BusinessException(message);
        }

        SyncJobRunResponse response = new SyncJobRunResponse();
        response.setLogId(logId);
        response.setJobCode(JOB_CODE);
        response.setRunStatus(runStatus);
        response.setMessage(message);
        response.setSyncedOrderCount(syncedOrderCount);
        response.setSyncedPaymentOrderCount(syncedPaymentOrderCount);
        response.setSyncedNotifyLogCount(syncedNotifyLogCount);
        response.setStartedAt(startedAt);
        response.setFinishedAt(finishedAt);
        response.setDurationMs(durationMs);
        return response;
    }

    public List<SyncJobLogResponse> queryLatestLogs(int limit) {
        return warehouseDorisMapper.queryLatestSyncJobLogs(JOB_CODE, limit);
    }

    private long syncOrders() {
        long total = 0L;
        long lastId = 0L;
        while (true) {
            List<BizOrderSourceRow> batch = sourceBizOrderMapper.selectBatchAfterId(lastId, BATCH_SIZE);
            if (batch.isEmpty()) {
                return total;
            }
            warehouseDorisMapper.insertOdsWxOrders(batch);
            total += batch.size();
            lastId = batch.get(batch.size() - 1).getId();
        }
    }

    private long syncPaymentOrders() {
        long total = 0L;
        long lastId = 0L;
        while (true) {
            List<BizPaymentOrderSourceRow> batch = sourceBizPaymentOrderMapper.selectBatchAfterId(lastId, BATCH_SIZE);
            if (batch.isEmpty()) {
                return total;
            }
            warehouseDorisMapper.insertOdsWxPaymentOrders(batch);
            total += batch.size();
            lastId = batch.get(batch.size() - 1).getId();
        }
    }

    private long syncPaymentNotifyLogs() {
        long total = 0L;
        long lastId = 0L;
        while (true) {
            List<BizPaymentNotifyLogSourceRow> batch = sourceBizPaymentNotifyLogMapper.selectBatchAfterId(lastId, BATCH_SIZE);
            if (batch.isEmpty()) {
                return total;
            }
            warehouseDorisMapper.insertOdsWxPaymentNotifyLogs(batch);
            total += batch.size();
            lastId = batch.get(batch.size() - 1).getId();
        }
    }

    private void ensureMetaTables() {
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

    private void saveSyncJob(DwSyncJobEntity job) {
        warehouseDorisMapper.saveSyncJob(job);
    }

    private void saveSyncJobLog(DwSyncJobLogEntity log) {
        warehouseDorisMapper.saveSyncJobLog(log);
    }

    private DwSyncJobEntity buildSyncJob(String runStatus, String message, Instant finishedAt) {
        DwSyncJobEntity job = new DwSyncJobEntity();
        job.setJobCode(JOB_CODE);
        job.setJobName(JOB_NAME);
        job.setSourceType("POSTGRESQL");
        job.setSourceTables("biz_order,biz_payment_order,biz_payment_notify_log");
        job.setTargetTables("ods_wx_order,ods_wx_payment_order,ods_wx_payment_notify");
        job.setEnabled(1);
        job.setRemark("Manual MVP sync job from order-service PostgreSQL to Doris ODS");
        job.setLastRunStatus(runStatus);
        job.setLastRunMessage(truncate(message, 255));
        job.setLastRunAt(Timestamp.from(finishedAt));
        job.setCreatedAt(Timestamp.from(finishedAt));
        job.setUpdatedAt(Timestamp.from(finishedAt));
        return job;
    }

    private DwSyncJobLogEntity buildSyncJobLog(
        String logId,
        String runStatus,
        String message,
        long syncedOrderCount,
        long syncedPaymentOrderCount,
        long syncedNotifyLogCount,
        Instant startedAt,
        Instant finishedAt,
        long durationMs
    ) {
        DwSyncJobLogEntity log = new DwSyncJobLogEntity();
        log.setLogId(logId);
        log.setJobCode(JOB_CODE);
        log.setJobName(JOB_NAME);
        log.setRunStatus(runStatus);
        log.setMessage(truncate(message, 255));
        log.setSyncedOrderCount(syncedOrderCount);
        log.setSyncedPaymentOrderCount(syncedPaymentOrderCount);
        log.setSyncedNotifyLogCount(syncedNotifyLogCount);
        log.setStartedAt(Timestamp.from(startedAt));
        log.setFinishedAt(Timestamp.from(finishedAt));
        log.setDurationMs(durationMs);
        log.setCreatedAt(Timestamp.from(finishedAt));
        return log;
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
