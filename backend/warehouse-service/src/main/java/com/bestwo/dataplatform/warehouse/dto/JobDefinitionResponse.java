package com.bestwo.dataplatform.warehouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JobDefinitionResponse {

    @JsonProperty("job_code")
    private String jobCode;

    @JsonProperty("job_name")
    private String jobName;

    @JsonProperty("source_type")
    private String sourceType;

    @JsonProperty("source_tables")
    private String sourceTables;

    @JsonProperty("target_tables")
    private String targetTables;

    @JsonProperty("enabled")
    private Integer enabled;

    @JsonProperty("remark")
    private String remark;

    @JsonProperty("last_run_status")
    private String lastRunStatus;

    @JsonProperty("last_run_message")
    private String lastRunMessage;

    @JsonProperty("last_run_at")
    private String lastRunAt;

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

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getLastRunStatus() {
        return lastRunStatus;
    }

    public void setLastRunStatus(String lastRunStatus) {
        this.lastRunStatus = lastRunStatus;
    }

    public String getLastRunMessage() {
        return lastRunMessage;
    }

    public void setLastRunMessage(String lastRunMessage) {
        this.lastRunMessage = lastRunMessage;
    }

    public String getLastRunAt() {
        return lastRunAt;
    }

    public void setLastRunAt(String lastRunAt) {
        this.lastRunAt = lastRunAt;
    }
}
