package com.bestwo.dataplatform.warehouse.entity;

import java.sql.Timestamp;

public class DwJobLogEntity {

    private String logId;
    private String jobCode;
    private String jobName;
    private String jobType;
    private String sourceType;
    private String sourceTables;
    private String targetTables;
    private String runStatus;
    private String message;
    private String metricOneLabel;
    private Long metricOneValue;
    private String metricTwoLabel;
    private Long metricTwoValue;
    private String metricThreeLabel;
    private Long metricThreeValue;
    private Timestamp startedAt;
    private Timestamp finishedAt;
    private Long durationMs;
    private Timestamp createdAt;

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getJobCode() {
        return jobCode;
    }

    public void setJobCode(String jobCode) {
        this.jobCode = jobCode;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceTables() {
        return sourceTables;
    }

    public void setSourceTables(String sourceTables) {
        this.sourceTables = sourceTables;
    }

    public String getTargetTables() {
        return targetTables;
    }

    public void setTargetTables(String targetTables) {
        this.targetTables = targetTables;
    }

    public String getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMetricOneLabel() {
        return metricOneLabel;
    }

    public void setMetricOneLabel(String metricOneLabel) {
        this.metricOneLabel = metricOneLabel;
    }

    public Long getMetricOneValue() {
        return metricOneValue;
    }

    public void setMetricOneValue(Long metricOneValue) {
        this.metricOneValue = metricOneValue;
    }

    public String getMetricTwoLabel() {
        return metricTwoLabel;
    }

    public void setMetricTwoLabel(String metricTwoLabel) {
        this.metricTwoLabel = metricTwoLabel;
    }

    public Long getMetricTwoValue() {
        return metricTwoValue;
    }

    public void setMetricTwoValue(Long metricTwoValue) {
        this.metricTwoValue = metricTwoValue;
    }

    public String getMetricThreeLabel() {
        return metricThreeLabel;
    }

    public void setMetricThreeLabel(String metricThreeLabel) {
        this.metricThreeLabel = metricThreeLabel;
    }

    public Long getMetricThreeValue() {
        return metricThreeValue;
    }

    public void setMetricThreeValue(Long metricThreeValue) {
        this.metricThreeValue = metricThreeValue;
    }

    public Timestamp getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Timestamp startedAt) {
        this.startedAt = startedAt;
    }

    public Timestamp getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Timestamp finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
