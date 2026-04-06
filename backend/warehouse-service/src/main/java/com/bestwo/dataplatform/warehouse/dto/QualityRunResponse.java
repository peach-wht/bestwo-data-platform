package com.bestwo.dataplatform.warehouse.dto;

import java.time.Instant;

public class QualityRunResponse {

    private String logId;
    private String jobCode;
    private String runStatus;
    private String message;
    private Long checkedRuleCount;
    private Long failedRuleCount;
    private Long passedRuleCount;
    private Instant startedAt;
    private Instant finishedAt;
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

    public Long getCheckedRuleCount() {
        return checkedRuleCount;
    }

    public void setCheckedRuleCount(Long checkedRuleCount) {
        this.checkedRuleCount = checkedRuleCount;
    }

    public Long getFailedRuleCount() {
        return failedRuleCount;
    }

    public void setFailedRuleCount(Long failedRuleCount) {
        this.failedRuleCount = failedRuleCount;
    }

    public Long getPassedRuleCount() {
        return passedRuleCount;
    }

    public void setPassedRuleCount(Long passedRuleCount) {
        this.passedRuleCount = passedRuleCount;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }
}
