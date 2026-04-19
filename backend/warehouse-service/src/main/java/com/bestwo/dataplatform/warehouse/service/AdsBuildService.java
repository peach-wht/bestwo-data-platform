package com.bestwo.dataplatform.warehouse.service;

import com.bestwo.dataplatform.common.exception.BusinessException;
import com.bestwo.dataplatform.warehouse.dto.AdsBuildRunResponse;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class AdsBuildService {

    private static final Logger log = LoggerFactory.getLogger(AdsBuildService.class);
    private static final String JOB_CODE = "build-ads-order-metrics";
    private static final String JOB_NAME = "Build DWS and ADS order metrics from DWD";
    private static final String DWD_TABLE_NAME = "dwd_wx_order_detail";
    private static final String DWS_TABLE_NAME = "dws_wx_pay_trade_day";
    private static final String ADS_DAY_TABLE_NAME = "ads_order_day_summary";
    private static final String ADS_OVERVIEW_TABLE_NAME = "ads_pay_dashboard_overview";
    private static final List<String> META_SCHEMA_RESOURCES = List.of(
        "sql/doris/meta/01_create_dw_sync_job.sql",
        "sql/doris/meta/02_create_dw_sync_job_log.sql",
        "sql/doris/meta/06_create_dw_job_log.sql"
    );
    private static final List<String> TARGET_SCHEMA_RESOURCES = List.of(
        "sql/doris/dws/01_create_dws_wx_pay_trade_day.sql",
        "sql/doris/ads/01_create_ads_order_day_summary.sql",
        "sql/doris/ads/02_create_ads_pay_dashboard_overview.sql"
    );

    private final WarehouseDorisMapper warehouseDorisMapper;

    public AdsBuildService(WarehouseDorisMapper warehouseDorisMapper) {
        this.warehouseDorisMapper = warehouseDorisMapper;
    }

    public AdsBuildRunResponse runBuild() {
        ensureMetaTables();
        ensureTargetTables();
        assertDwdTableReady();

        Instant startedAt = Instant.now();
        String logId = "ADS-" + System.currentTimeMillis();
        log.info("ads build started {}", StructuredArguments.entries(buildJobFields("ads_build_started", logId, null, null)));
        String runStatus = "SUCCESS";
        String message = "ads build completed";
        Long dwsRowCount = 0L;
        Long adsDayRowCount = 0L;
        Long adsOverviewRowCount = 0L;

        try {
            rebuildDwsTable();
            rebuildAdsOrderDaySummary();
            rebuildAdsDashboardOverview();

            dwsRowCount = warehouseDorisMapper.countRows(DWS_TABLE_NAME);
            adsDayRowCount = warehouseDorisMapper.countRows(ADS_DAY_TABLE_NAME);
            adsOverviewRowCount = warehouseDorisMapper.countRows(ADS_OVERVIEW_TABLE_NAME);
            message = "ads build completed, dws=" + dwsRowCount + ", ads_day=" + adsDayRowCount + ", ads_overview=" + adsOverviewRowCount;
        } catch (RuntimeException exception) {
            runStatus = "FAILED";
            message = exception.getMessage() == null ? "ads build failed" : exception.getMessage();
        }

        Instant finishedAt = Instant.now();
        long durationMs = finishedAt.toEpochMilli() - startedAt.toEpochMilli();

        saveJob(buildJob(runStatus, message, finishedAt));
        saveJobLog(buildJobLog(
            logId,
            runStatus,
            message,
            dwsRowCount,
            adsDayRowCount,
            adsOverviewRowCount,
            startedAt,
            finishedAt,
            durationMs
        ));
        saveDwJobLog(buildDwJobLog(
            logId,
            runStatus,
            message,
            dwsRowCount,
            adsDayRowCount,
            adsOverviewRowCount,
            startedAt,
            finishedAt,
            durationMs
        ));

        if (!"SUCCESS".equals(runStatus)) {
            log.error("ads build failed {}", StructuredArguments.entries(buildJobFields("ads_build_failed", logId, runStatus, durationMs)));
            throw new BusinessException(message);
        }

        AdsBuildRunResponse response = new AdsBuildRunResponse();
        response.setLogId(logId);
        response.setJobCode(JOB_CODE);
        response.setRunStatus(runStatus);
        response.setMessage(message);
        response.setDwsRowCount(dwsRowCount);
        response.setAdsOrderDaySummaryRowCount(adsDayRowCount);
        response.setAdsDashboardOverviewRowCount(adsOverviewRowCount);
        response.setStartedAt(startedAt);
        response.setFinishedAt(finishedAt);
        response.setDurationMs(durationMs);
        log.info("ads build completed {}", StructuredArguments.entries(buildJobFields("ads_build_completed", logId, runStatus, durationMs)));
        return response;
    }

    public List<SyncJobLogResponse> queryLatestLogs(int limit) {
        return warehouseDorisMapper.queryLatestSyncJobLogs(JOB_CODE, limit);
    }

    private void rebuildDwsTable() {
        warehouseDorisMapper.executeSql("TRUNCATE TABLE " + DWS_TABLE_NAME);
        warehouseDorisMapper.executeSql("""
            INSERT INTO dws_wx_pay_trade_day (
                stat_date,
                order_count,
                paid_order_count,
                unpaid_order_count,
                closed_order_count,
                total_amount,
                paid_amount,
                refund_amount,
                pay_success_rate,
                sync_time
            )
            SELECT
                stat_date,
                COUNT(1) AS order_count,
                SUM(CASE WHEN pay_status = 'PAID' THEN 1 ELSE 0 END) AS paid_order_count,
                SUM(CASE WHEN pay_status IN ('UNPAID', 'PAYING') THEN 1 ELSE 0 END) AS unpaid_order_count,
                SUM(CASE WHEN order_status = 'CLOSED' THEN 1 ELSE 0 END) AS closed_order_count,
                SUM(COALESCE(total_amount, 0)) AS total_amount,
                SUM(COALESCE(paid_amount, 0)) AS paid_amount,
                SUM(COALESCE(refund_amount, 0)) AS refund_amount,
                CASE
                    WHEN COUNT(1) = 0 THEN 0
                    ELSE ROUND(SUM(CASE WHEN pay_status = 'PAID' THEN 1 ELSE 0 END) / COUNT(1), 4)
                END AS pay_success_rate,
                NOW() AS sync_time
            FROM dwd_wx_order_detail
            GROUP BY stat_date
            """);
    }

    private void rebuildAdsOrderDaySummary() {
        warehouseDorisMapper.executeSql("TRUNCATE TABLE " + ADS_DAY_TABLE_NAME);
        warehouseDorisMapper.executeSql("""
            INSERT INTO ads_order_day_summary (
                stat_date,
                order_count,
                paid_order_count,
                total_amount,
                paid_amount,
                refund_amount,
                sync_time
            )
            SELECT
                stat_date,
                order_count,
                paid_order_count,
                total_amount,
                paid_amount,
                refund_amount,
                NOW() AS sync_time
            FROM dws_wx_pay_trade_day
            """);
    }

    private void rebuildAdsDashboardOverview() {
        warehouseDorisMapper.executeSql("TRUNCATE TABLE " + ADS_OVERVIEW_TABLE_NAME);
        warehouseDorisMapper.executeSql("""
            INSERT INTO ads_pay_dashboard_overview (
                metric_scope,
                latest_stat_date,
                order_count,
                paid_order_count,
                unpaid_order_count,
                total_amount,
                paid_amount,
                refund_amount,
                pay_success_rate,
                sync_time
            )
            SELECT
                'ALL' AS metric_scope,
                MAX(stat_date) AS latest_stat_date,
                SUM(order_count) AS order_count,
                SUM(paid_order_count) AS paid_order_count,
                SUM(unpaid_order_count) AS unpaid_order_count,
                SUM(total_amount) AS total_amount,
                SUM(paid_amount) AS paid_amount,
                SUM(refund_amount) AS refund_amount,
                CASE
                    WHEN SUM(order_count) = 0 THEN 0
                    ELSE ROUND(SUM(paid_order_count) / SUM(order_count), 4)
                END AS pay_success_rate,
                NOW() AS sync_time
            FROM dws_wx_pay_trade_day
            """);
    }

    private void assertDwdTableReady() {
        try {
            Long count = warehouseDorisMapper.countRows(DWD_TABLE_NAME);
            if (count == null) {
                throw new BusinessException("dwd_wx_order_detail table not found, please run build-dwd first");
            }
        } catch (RuntimeException exception) {
            throw new BusinessException("dwd_wx_order_detail is not ready, please run build-dwd first");
        }
    }

    private void ensureMetaTables() {
        for (String resourcePath : META_SCHEMA_RESOURCES) {
            executeSqlResource(resourcePath);
        }
    }

    private void ensureTargetTables() {
        for (String resourcePath : TARGET_SCHEMA_RESOURCES) {
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
        job.setSourceTables(DWD_TABLE_NAME);
        job.setTargetTables(DWS_TABLE_NAME + "," + ADS_DAY_TABLE_NAME + "," + ADS_OVERVIEW_TABLE_NAME);
        job.setEnabled(1);
        job.setRemark("Manual MVP ADS build job from DWD to DWS/ADS");
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
        Long dwsRowCount,
        Long adsDayRowCount,
        Long adsOverviewRowCount,
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
        log.setSyncedOrderCount(dwsRowCount);
        log.setSyncedPaymentOrderCount(adsDayRowCount);
        log.setSyncedNotifyLogCount(adsOverviewRowCount);
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
        Long dwsRowCount,
        Long adsDayRowCount,
        Long adsOverviewRowCount,
        Instant startedAt,
        Instant finishedAt,
        long durationMs
    ) {
        DwJobLogEntity log = new DwJobLogEntity();
        log.setLogId(logId);
        log.setJobCode(JOB_CODE);
        log.setJobName(JOB_NAME);
        log.setJobType("BUILD_ADS");
        log.setSourceType("DORIS");
        log.setSourceTables(DWD_TABLE_NAME);
        log.setTargetTables(DWS_TABLE_NAME + "," + ADS_DAY_TABLE_NAME + "," + ADS_OVERVIEW_TABLE_NAME);
        log.setRunStatus(runStatus);
        log.setMessage(truncate(message, 255));
        log.setMetricOneLabel("dwsRowCount");
        log.setMetricOneValue(dwsRowCount);
        log.setMetricTwoLabel("adsOrderDaySummaryRowCount");
        log.setMetricTwoValue(adsDayRowCount);
        log.setMetricThreeLabel("adsDashboardOverviewRowCount");
        log.setMetricThreeValue(adsOverviewRowCount);
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

    private Map<String, Object> buildJobFields(String event, String logId, String runStatus, Long durationMs) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("event", event);
        fields.put("jobCode", JOB_CODE);
        fields.put("logId", logId);
        if (runStatus != null) {
            fields.put("runStatus", runStatus);
        }
        if (durationMs != null) {
            fields.put("durationMs", durationMs);
        }
        return fields;
    }
}
