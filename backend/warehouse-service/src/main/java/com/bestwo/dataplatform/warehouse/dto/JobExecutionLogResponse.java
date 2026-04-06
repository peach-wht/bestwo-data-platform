package com.bestwo.dataplatform.warehouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JobExecutionLogResponse {

    @JsonProperty("log_id")
    private String logId;

    @JsonProperty("job_code")
    private String jobCode;

    @JsonProperty("job_name")
    private String jobName;

    @JsonProperty("job_type")
    private String jobType;

    @JsonProperty("source_type")
    private String sourceType;

    @JsonProperty("source_tables")
    private String sourceTables;

    @JsonProperty("target_tables")
    private String targetTables;

    @JsonProperty("run_status")
    private String runStatus;

    @JsonProperty("message")
    private String message;

    @JsonProperty("metric_one_label")
    private String metricOneLabel;

    @JsonProperty("metric_one_value")
    private Long metricOneValue;

    @JsonProperty("metric_two_label")
    private String metricTwoLabel;

    @JsonProperty("metric_two_value")
    private Long metricTwoValue;

    @JsonProperty("metric_three_label")
    private String metricThreeLabel;

    @JsonProperty("metric_three_value")
    private Long metricThreeValue;

    @JsonProperty("started_at")
    private String startedAt;

    @JsonProperty("finished_at")
    private String finishedAt;

    @JsonProperty("duration_ms")
    private Long durationMs;

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

    public String getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    public String getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(String finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }
}
