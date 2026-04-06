package com.bestwo.dataplatform.warehouse.service;

import com.bestwo.dataplatform.common.exception.BusinessException;
import com.bestwo.dataplatform.warehouse.dto.JobDefinitionResponse;
import com.bestwo.dataplatform.warehouse.dto.JobExecutionLogResponse;
import com.bestwo.dataplatform.warehouse.dto.QualityRuleResponse;
import com.bestwo.dataplatform.warehouse.dto.QualityRunResponse;
import com.bestwo.dataplatform.warehouse.entity.DwAlertRecordEntity;
import com.bestwo.dataplatform.warehouse.entity.DwJobLogEntity;
import com.bestwo.dataplatform.warehouse.entity.DwQualityResultEntity;
import com.bestwo.dataplatform.warehouse.entity.DwQualityRuleEntity;
import com.bestwo.dataplatform.warehouse.entity.DwSyncJobEntity;
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
public class QualityCheckService {

    private static final String JOB_CODE = "run-quality-check";
    private static final String JOB_NAME = "Run warehouse quality checks";
    private static final List<String> META_SCHEMA_RESOURCES = List.of(
        "sql/doris/meta/01_create_dw_sync_job.sql",
        "sql/doris/meta/03_create_dw_meta_datasource.sql",
        "sql/doris/meta/04_create_dw_meta_table.sql",
        "sql/doris/meta/05_create_dw_meta_column.sql",
        "sql/doris/meta/06_create_dw_job_log.sql",
        "sql/doris/meta/07_create_dw_quality_rule.sql",
        "sql/doris/meta/08_create_dw_quality_result.sql",
        "sql/doris/meta/09_create_dw_lineage_relation.sql",
        "sql/doris/meta/10_create_dw_alert_record.sql"
    );

    private final WarehouseDorisMapper warehouseDorisMapper;

    public QualityCheckService(WarehouseDorisMapper warehouseDorisMapper) {
        this.warehouseDorisMapper = warehouseDorisMapper;
    }

    public QualityRunResponse runQualityChecks() {
        ensureQualityTables();
        ensureDefaultRules();
        ensureQualityJobDefinition();

        List<QualityRuleResponse> rules = warehouseDorisMapper.queryQualityRules(100);
        if (rules.isEmpty()) {
            throw new BusinessException("no quality rules configured");
        }

        Instant startedAt = Instant.now();
        String logId = "QUALITY-" + System.currentTimeMillis();
        String runStatus = "SUCCESS";
        String message = "quality check completed";
        long checkedRuleCount = 0L;
        long failedRuleCount = 0L;
        long passedRuleCount = 0L;

        try {
            int index = 0;
            for (QualityRuleResponse rule : rules) {
                if (rule.getEnabled() == null || rule.getEnabled() != 1) {
                    continue;
                }
                index++;
                checkedRuleCount++;
                RuleExecutionOutcome outcome = executeRule(rule, index, startedAt);
                warehouseDorisMapper.saveQualityResult(outcome.result());
                saveAlertIfNecessary(outcome.result());
                if ("PASSED".equals(outcome.result().getResultStatus())) {
                    passedRuleCount++;
                } else {
                    failedRuleCount++;
                }
            }
            message = "quality check completed, checked=" + checkedRuleCount + ", failed=" + failedRuleCount;
        } catch (RuntimeException exception) {
            runStatus = "FAILED";
            message = exception.getMessage() == null ? "quality check failed" : exception.getMessage();
        }

        Instant finishedAt = Instant.now();
        long durationMs = finishedAt.toEpochMilli() - startedAt.toEpochMilli();

        warehouseDorisMapper.saveDwJobLog(buildJobLog(
            logId,
            runStatus,
            message,
            checkedRuleCount,
            failedRuleCount,
            passedRuleCount,
            startedAt,
            finishedAt,
            durationMs
        ));

        if (!"SUCCESS".equals(runStatus)) {
            warehouseDorisMapper.saveAlertRecord(buildJobFailureAlert(logId, message, finishedAt));
        }

        if (!"SUCCESS".equals(runStatus)) {
            throw new BusinessException(message);
        }

        QualityRunResponse response = new QualityRunResponse();
        response.setLogId(logId);
        response.setJobCode(JOB_CODE);
        response.setRunStatus(runStatus);
        response.setMessage(message);
        response.setCheckedRuleCount(checkedRuleCount);
        response.setFailedRuleCount(failedRuleCount);
        response.setPassedRuleCount(passedRuleCount);
        response.setStartedAt(startedAt);
        response.setFinishedAt(finishedAt);
        response.setDurationMs(durationMs);
        return response;
    }

    public List<QualityRuleResponse> queryRules(int limit) {
        ensureQualityTables();
        ensureDefaultRules();
        return warehouseDorisMapper.queryQualityRules(limit);
    }

    public List<JobExecutionLogResponse> queryLatestLogs(int limit) {
        ensureQualityTables();
        return warehouseDorisMapper.queryDwJobLogs(JOB_CODE, limit);
    }

    private RuleExecutionOutcome executeRule(QualityRuleResponse rule, int index, Instant startedAt) {
        Timestamp checkedAt = Timestamp.from(Instant.now());
        DwQualityResultEntity result = new DwQualityResultEntity();
        result.setResultId("QR-" + startedAt.toEpochMilli() + "-" + index);
        result.setRuleCode(rule.getRuleCode());
        result.setRuleName(rule.getRuleName());
        result.setTableName(rule.getTableName());
        result.setResultLevel(rule.getRuleLevel());
        result.setThresholdValue(rule.getThresholdValue());
        result.setCheckedAt(checkedAt);
        result.setCreatedAt(checkedAt);

        try {
            Long failedCount = safeLong(warehouseDorisMapper.queryLongBySql(resolveRuleSql(rule.getRuleCode())));
            Long totalCount = safeLong(warehouseDorisMapper.queryLongBySql(resolveTotalSql(rule.getRuleCode())));
            long passCount = Math.max(0L, totalCount - failedCount);
            result.setFailedCount(failedCount);
            result.setTotalCount(totalCount);
            result.setPassCount(passCount);
            result.setResultStatus(failedCount > 0 ? "FAILED" : "PASSED");
            result.setMessage(truncate("failed=" + failedCount + ", total=" + totalCount, 255));
            return new RuleExecutionOutcome(result);
        } catch (RuntimeException exception) {
            result.setFailedCount(0L);
            result.setTotalCount(0L);
            result.setPassCount(0L);
            result.setResultStatus("ERROR");
            result.setMessage(truncate(exception.getMessage(), 255));
            return new RuleExecutionOutcome(result);
        }
    }

    private void ensureQualityTables() {
        for (String resourcePath : META_SCHEMA_RESOURCES) {
            executeSqlResource(resourcePath);
        }
    }

    private void saveAlertIfNecessary(DwQualityResultEntity result) {
        if ("PASSED".equals(result.getResultStatus())) {
            return;
        }
        warehouseDorisMapper.saveAlertRecord(buildQualityAlert(result));
    }

    private void ensureDefaultRules() {
        if (!warehouseDorisMapper.queryQualityRules(10).isEmpty()) {
            return;
        }

        Timestamp now = Timestamp.from(Instant.now());
        List<DwQualityRuleEntity> rules = new ArrayList<>();
        rules.add(buildRule(
            "Q001",
            "ODS order duplicate primary key",
            "ods_wx_order",
            "HIGH",
            "SQL",
            1,
            "Check duplicate order_id in ods_wx_order",
            now
        ));
        rules.add(buildRule(
            "Q002",
            "Paid orders missing pay time",
            "dwd_wx_order_detail",
            "HIGH",
            "SQL",
            2,
            "Check paid orders without pay_time in DWD",
            now
        ));
        rules.add(buildRule(
            "Q003",
            "Successful notify without business order",
            "ods_wx_payment_notify",
            "MEDIUM",
            "SQL",
            3,
            "Check successful payment notify records without matching order",
            now
        ));
        rules.add(buildRule(
            "Q004",
            "ADS negative amount check",
            "ads_order_day_summary",
            "HIGH",
            "SQL",
            4,
            "Check negative amounts in ads_order_day_summary",
            now
        ));
        warehouseDorisMapper.saveQualityRules(rules);
    }

    private void ensureQualityJobDefinition() {
        boolean exists = false;
        for (JobDefinitionResponse job : warehouseDorisMapper.queryJobDefinitions(100)) {
            if (JOB_CODE.equals(job.getJobCode())) {
                exists = true;
                break;
            }
        }
        if (exists) {
            return;
        }

        Instant now = Instant.now();
        DwSyncJobEntity job = new DwSyncJobEntity();
        job.setJobCode(JOB_CODE);
        job.setJobName(JOB_NAME);
        job.setSourceType("DORIS");
        job.setSourceTables("ods_wx_order,dwd_wx_order_detail,ods_wx_payment_notify,ads_order_day_summary");
        job.setTargetTables("dw_quality_result");
        job.setEnabled(1);
        job.setRemark("Manual quality check job for MVP warehouse governance");
        job.setLastRunStatus("NOT_RUN");
        job.setLastRunMessage("quality job initialized");
        job.setLastRunAt(Timestamp.from(now));
        job.setCreatedAt(Timestamp.from(now));
        job.setUpdatedAt(Timestamp.from(now));
        warehouseDorisMapper.saveSyncJob(job);
    }

    private DwQualityRuleEntity buildRule(
        String ruleCode,
        String ruleName,
        String tableName,
        String ruleLevel,
        String ruleType,
        int ruleOrder,
        String description,
        Timestamp now
    ) {
        DwQualityRuleEntity entity = new DwQualityRuleEntity();
        entity.setRuleCode(ruleCode);
        entity.setRuleName(ruleName);
        entity.setTableName(tableName);
        entity.setRuleLevel(ruleLevel);
        entity.setRuleType(ruleType);
        entity.setRuleSql(resolveRuleSql(ruleCode));
        entity.setTotalSql(resolveTotalSql(ruleCode));
        entity.setThresholdValue(0L);
        entity.setEnabled(1);
        entity.setRuleOrder(ruleOrder);
        entity.setDescription(description);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return entity;
    }

    private DwJobLogEntity buildJobLog(
        String logId,
        String runStatus,
        String message,
        long checkedRuleCount,
        long failedRuleCount,
        long passedRuleCount,
        Instant startedAt,
        Instant finishedAt,
        long durationMs
    ) {
        DwJobLogEntity log = new DwJobLogEntity();
        log.setLogId(logId);
        log.setJobCode(JOB_CODE);
        log.setJobName(JOB_NAME);
        log.setJobType("QUALITY");
        log.setSourceType("DORIS");
        log.setSourceTables("ods_wx_order,dwd_wx_order_detail,ods_wx_payment_notify,ads_order_day_summary");
        log.setTargetTables("dw_quality_result");
        log.setRunStatus(runStatus);
        log.setMessage(truncate(message, 255));
        log.setMetricOneLabel("checkedRuleCount");
        log.setMetricOneValue(checkedRuleCount);
        log.setMetricTwoLabel("failedRuleCount");
        log.setMetricTwoValue(failedRuleCount);
        log.setMetricThreeLabel("passedRuleCount");
        log.setMetricThreeValue(passedRuleCount);
        log.setStartedAt(Timestamp.from(startedAt));
        log.setFinishedAt(Timestamp.from(finishedAt));
        log.setDurationMs(durationMs);
        log.setCreatedAt(Timestamp.from(finishedAt));
        return log;
    }

    private DwAlertRecordEntity buildQualityAlert(DwQualityResultEntity result) {
        DwAlertRecordEntity alert = new DwAlertRecordEntity();
        alert.setAlertId("ALERT-" + result.getResultId());
        alert.setAlertType("QUALITY_CHECK");
        alert.setAlertLevel(defaultIfBlank(result.getResultLevel(), "MEDIUM"));
        alert.setAlertSource("dw_quality_result");
        alert.setSourceCode(result.getRuleCode());
        alert.setSourceName(result.getRuleName());
        alert.setAlertStatus("OPEN");
        alert.setAlertTitle(truncate("Quality rule failed: " + result.getRuleName(), 128));
        alert.setAlertMessage(truncate(
            "table=" + result.getTableName() + ", status=" + result.getResultStatus() + ", " + result.getMessage(),
            255
        ));
        alert.setFiredAt(result.getCheckedAt());
        alert.setResolvedAt(null);
        alert.setCreatedAt(result.getCreatedAt());
        return alert;
    }

    private DwAlertRecordEntity buildJobFailureAlert(String logId, String message, Instant finishedAt) {
        Timestamp finishedAtTs = Timestamp.from(finishedAt);
        DwAlertRecordEntity alert = new DwAlertRecordEntity();
        alert.setAlertId("ALERT-JOB-" + logId);
        alert.setAlertType("JOB_FAILURE");
        alert.setAlertLevel("HIGH");
        alert.setAlertSource("dw_job_log");
        alert.setSourceCode(JOB_CODE);
        alert.setSourceName(JOB_NAME);
        alert.setAlertStatus("OPEN");
        alert.setAlertTitle("Warehouse quality job failed");
        alert.setAlertMessage(truncate(message, 255));
        alert.setFiredAt(finishedAtTs);
        alert.setResolvedAt(null);
        alert.setCreatedAt(finishedAtTs);
        return alert;
    }

    private String resolveRuleSql(String ruleCode) {
        return switch (ruleCode) {
            case "Q001" -> """
                SELECT COUNT(1)
                FROM (
                    SELECT order_id
                    FROM ods_wx_order
                    GROUP BY order_id
                    HAVING COUNT(1) > 1
                ) t
                """;
            case "Q002" -> """
                SELECT COUNT(1)
                FROM dwd_wx_order_detail
                WHERE pay_status = 'PAID'
                  AND pay_time IS NULL
                """;
            case "Q003" -> """
                SELECT COUNT(1)
                FROM ods_wx_payment_notify n
                LEFT JOIN ods_wx_order o
                  ON n.order_id = o.order_id
                WHERE n.event_status = 'SUCCESS'
                  AND o.order_id IS NULL
                """;
            case "Q004" -> """
                SELECT COUNT(1)
                FROM ads_order_day_summary
                WHERE total_amount < 0
                   OR paid_amount < 0
                   OR refund_amount < 0
                """;
            default -> throw new BusinessException("unknown quality rule code: " + ruleCode);
        };
    }

    private String resolveTotalSql(String ruleCode) {
        return switch (ruleCode) {
            case "Q001" -> "SELECT COUNT(1) FROM ods_wx_order";
            case "Q002" -> "SELECT COUNT(1) FROM dwd_wx_order_detail WHERE pay_status = 'PAID'";
            case "Q003" -> "SELECT COUNT(1) FROM ods_wx_payment_notify WHERE event_status = 'SUCCESS'";
            case "Q004" -> "SELECT COUNT(1) FROM ads_order_day_summary";
            default -> throw new BusinessException("unknown quality rule code: " + ruleCode);
        };
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

    private Long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String defaultIfBlank(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    private record RuleExecutionOutcome(DwQualityResultEntity result) {
    }
}
