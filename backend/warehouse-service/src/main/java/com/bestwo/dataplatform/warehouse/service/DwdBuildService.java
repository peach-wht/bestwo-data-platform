package com.bestwo.dataplatform.warehouse.service;

import com.bestwo.dataplatform.common.exception.BusinessException;
import com.bestwo.dataplatform.warehouse.dto.DwBuildRunResponse;
import com.bestwo.dataplatform.warehouse.dto.SyncJobLogResponse;
import com.bestwo.dataplatform.warehouse.entity.DwJobLogEntity;
import com.bestwo.dataplatform.warehouse.entity.DwSyncJobEntity;
import com.bestwo.dataplatform.warehouse.entity.DwSyncJobLogEntity;
import com.bestwo.dataplatform.warehouse.mapper.WarehouseDorisMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class DwdBuildService {

    private static final String JOB_CODE = "build-dwd-order-detail";
    private static final String JOB_NAME = "Build DWD order detail from Doris ODS";
    private static final List<String> META_SCHEMA_RESOURCES = List.of(
        "sql/doris/meta/01_create_dw_sync_job.sql",
        "sql/doris/meta/02_create_dw_sync_job_log.sql",
        "sql/doris/meta/06_create_dw_job_log.sql"
    );
    private static final String DWD_SCHEMA_RESOURCE = "sql/doris/dwd/01_create_dwd_wx_order_detail.sql";
    private static final String DWD_TABLE_NAME = "dwd_wx_order_detail";

    private final WarehouseDorisMapper warehouseDorisMapper;
    private final DorisSchemaService dorisSchemaService;

    public DwdBuildService(WarehouseDorisMapper warehouseDorisMapper, DorisSchemaService dorisSchemaService) {
        this.warehouseDorisMapper = warehouseDorisMapper;
        this.dorisSchemaService = dorisSchemaService;
    }

    public DwBuildRunResponse runBuild() {
        dorisSchemaService.initOdsSchema();
        ensureMetaTables();
        ensureDwdTable();

        Instant startedAt = Instant.now();
        String logId = "DWD-" + System.currentTimeMillis();
        String runStatus = "SUCCESS";
        String message = "dwd build completed";
        Long outputRowCount = 0L;

        try {
            truncateDwdTable();
            insertDwdRows();
            outputRowCount = warehouseDorisMapper.countRows(DWD_TABLE_NAME);
            message = "dwd build completed, output rows=" + outputRowCount;
        } catch (RuntimeException exception) {
            runStatus = "FAILED";
            message = exception.getMessage() == null ? "dwd build failed" : exception.getMessage();
        }

        Instant finishedAt = Instant.now();
        long durationMs = finishedAt.toEpochMilli() - startedAt.toEpochMilli();

        saveJob(buildJob(runStatus, message, finishedAt));
        saveJobLog(buildJobLog(logId, runStatus, message, outputRowCount, startedAt, finishedAt, durationMs));
        saveDwJobLog(buildDwJobLog(logId, runStatus, message, outputRowCount, startedAt, finishedAt, durationMs));

        if (!"SUCCESS".equals(runStatus)) {
            throw new BusinessException(message);
        }

        DwBuildRunResponse response = new DwBuildRunResponse();
        response.setLogId(logId);
        response.setJobCode(JOB_CODE);
        response.setRunStatus(runStatus);
        response.setMessage(message);
        response.setOutputRowCount(outputRowCount);
        response.setStartedAt(startedAt);
        response.setFinishedAt(finishedAt);
        response.setDurationMs(durationMs);
        return response;
    }

    public List<SyncJobLogResponse> queryLatestLogs(int limit) {
        return warehouseDorisMapper.queryLatestSyncJobLogs(JOB_CODE, limit);
    }

    private void ensureMetaTables() {
        for (String resourcePath : META_SCHEMA_RESOURCES) {
            executeSqlResource(resourcePath);
        }
    }

    private void ensureDwdTable() {
        executeSqlResource(DWD_SCHEMA_RESOURCE);
    }

    private void truncateDwdTable() {
        warehouseDorisMapper.executeSql("TRUNCATE TABLE " + DWD_TABLE_NAME);
    }

    private void insertDwdRows() {
        warehouseDorisMapper.executeSql("""
            INSERT INTO dwd_wx_order_detail (
                order_id,
                order_no,
                external_order_no,
                biz_type,
                order_source,
                order_title,
                order_description,
                buyer_id,
                buyer_nickname,
                currency,
                total_amount_fen,
                payable_amount_fen,
                paid_amount_fen,
                refunded_amount_fen,
                total_amount,
                payable_amount,
                paid_amount,
                refund_amount,
                order_status,
                pay_status,
                preferred_pay_platform,
                preferred_trade_type,
                success_pay_platform,
                order_time,
                pay_time,
                expired_time,
                closed_time,
                cancelled_time,
                latest_payment_order_no,
                latest_channel_order_no,
                payment_order_id,
                payment_order_no,
                payment_platform,
                payment_trade_type,
                payment_status,
                payment_request_amount_fen,
                payment_success_amount_fen,
                payment_request_amount,
                payment_success_amount,
                channel_order_no,
                channel_prepay_id,
                code_url,
                payment_fail_code,
                payment_fail_message,
                payment_expired_time,
                payment_success_time,
                payment_closed_time,
                latest_notify_time,
                latest_notify_status,
                latest_notify_process_status,
                stat_date,
                sync_time
            )
            SELECT
                o.order_id,
                o.order_no,
                o.external_order_no,
                o.biz_type,
                o.order_source,
                o.order_title,
                o.order_description,
                o.buyer_id,
                o.buyer_nickname,
                o.currency,
                o.total_amount_fen,
                o.payable_amount_fen,
                o.paid_amount_fen,
                o.refunded_amount_fen,
                CAST(o.total_amount_fen AS DECIMAL(18, 2)) / 100,
                CAST(o.payable_amount_fen AS DECIMAL(18, 2)) / 100,
                CAST(o.paid_amount_fen AS DECIMAL(18, 2)) / 100,
                CAST(o.refunded_amount_fen AS DECIMAL(18, 2)) / 100,
                o.order_status,
                o.pay_status,
                o.preferred_pay_platform,
                o.preferred_trade_type,
                o.success_pay_platform,
                o.created_at AS order_time,
                o.paid_time AS pay_time,
                o.expired_time,
                o.closed_time,
                o.cancelled_time,
                o.latest_payment_order_no,
                o.latest_channel_order_no,
                p.payment_order_id,
                p.payment_order_no,
                p.platform AS payment_platform,
                p.trade_type AS payment_trade_type,
                p.status AS payment_status,
                p.request_amount_fen AS payment_request_amount_fen,
                p.success_amount_fen AS payment_success_amount_fen,
                CAST(p.request_amount_fen AS DECIMAL(18, 2)) / 100 AS payment_request_amount,
                CAST(p.success_amount_fen AS DECIMAL(18, 2)) / 100 AS payment_success_amount,
                p.channel_order_no,
                p.channel_prepay_id,
                p.code_url,
                p.fail_code AS payment_fail_code,
                p.fail_message AS payment_fail_message,
                p.expired_time AS payment_expired_time,
                p.success_time AS payment_success_time,
                p.closed_time AS payment_closed_time,
                n.latest_notify_time,
                n.latest_notify_status,
                n.latest_notify_process_status,
                DATE(o.created_at) AS stat_date,
                NOW() AS sync_time
            FROM ods_wx_order o
            LEFT JOIN ods_wx_payment_order p
                ON o.latest_payment_order_no = p.payment_order_no
            LEFT JOIN (
                SELECT
                    payment_order_no,
                    MAX(received_at) AS latest_notify_time,
                    MAX(event_status) AS latest_notify_status,
                    MAX(process_status) AS latest_notify_process_status
                FROM ods_wx_payment_notify
                GROUP BY payment_order_no
            ) n
                ON p.payment_order_no = n.payment_order_no
            """);
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

    private void saveJob(DwSyncJobEntity job) {
        warehouseDorisMapper.saveSyncJob(job);
    }

    private void saveJobLog(DwSyncJobLogEntity log) {
        warehouseDorisMapper.saveSyncJobLog(log);
    }

    private void saveDwJobLog(DwJobLogEntity log) {
        warehouseDorisMapper.saveDwJobLog(log);
    }

    private DwSyncJobEntity buildJob(String runStatus, String message, Instant finishedAt) {
        DwSyncJobEntity job = new DwSyncJobEntity();
        job.setJobCode(JOB_CODE);
        job.setJobName(JOB_NAME);
        job.setSourceType("DORIS");
        job.setSourceTables("ods_wx_order,ods_wx_payment_order,ods_wx_payment_notify");
        job.setTargetTables(DWD_TABLE_NAME);
        job.setEnabled(1);
        job.setRemark("Manual MVP DWD build job from ODS to dwd_wx_order_detail");
        job.setLastRunStatus(runStatus);
        job.setLastRunMessage(truncate(message, 255));
        job.setLastRunAt(Timestamp.from(finishedAt));
        job.setCreatedAt(Timestamp.from(finishedAt));
        job.setUpdatedAt(Timestamp.from(finishedAt));
        return job;
    }

    private DwSyncJobLogEntity buildJobLog(
        String logId,
        String runStatus,
        String message,
        Long outputRowCount,
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
        log.setSyncedOrderCount(outputRowCount);
        log.setSyncedPaymentOrderCount(0L);
        log.setSyncedNotifyLogCount(0L);
        log.setStartedAt(Timestamp.from(startedAt));
        log.setFinishedAt(Timestamp.from(finishedAt));
        log.setDurationMs(durationMs);
        log.setCreatedAt(Timestamp.from(finishedAt));
        return log;
    }

    private DwJobLogEntity buildDwJobLog(
        String logId,
        String runStatus,
        String message,
        Long outputRowCount,
        Instant startedAt,
        Instant finishedAt,
        long durationMs
    ) {
        DwJobLogEntity log = new DwJobLogEntity();
        log.setLogId(logId);
        log.setJobCode(JOB_CODE);
        log.setJobName(JOB_NAME);
        log.setJobType("BUILD_DWD");
        log.setSourceType("DORIS");
        log.setSourceTables("ods_wx_order,ods_wx_payment_order,ods_wx_payment_notify");
        log.setTargetTables(DWD_TABLE_NAME);
        log.setRunStatus(runStatus);
        log.setMessage(truncate(message, 255));
        log.setMetricOneLabel("outputRowCount");
        log.setMetricOneValue(outputRowCount);
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
