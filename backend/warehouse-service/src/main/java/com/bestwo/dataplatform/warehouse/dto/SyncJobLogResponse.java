package com.bestwo.dataplatform.warehouse.dto;

import java.time.Instant;

public class SyncJobLogResponse {

    private String logId;
    private String jobCode;
    private String jobName;
    private String runStatus;
    private String message;
    private Long syncedOrderCount;
    private Long syncedPaymentOrderCount;
    private Long syncedNotifyLogCount;
    private Instant startedAt;
    private Instant finishedAt;
    private Long durationMs;
    private Instant createdAt;

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

    public Long getSyncedOrderCount() {
        return syncedOrderCount;
    }

    public void setSyncedOrderCount(Long syncedOrderCount) {
        this.syncedOrderCount = syncedOrderCount;
    }

    public Long getSyncedPaymentOrderCount() {
        return syncedPaymentOrderCount;
    }

    public void setSyncedPaymentOrderCount(Long syncedPaymentOrderCount) {
        this.syncedPaymentOrderCount = syncedPaymentOrderCount;
    }

    public Long getSyncedNotifyLogCount() {
        return syncedNotifyLogCount;
    }

    public void setSyncedNotifyLogCount(Long syncedNotifyLogCount) {
        this.syncedNotifyLogCount = syncedNotifyLogCount;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
