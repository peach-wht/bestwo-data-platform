package com.bestwo.dataplatform.warehouse.dto;

import java.time.Instant;

public class SyncJobRunResponse {

    private String logId;
    private String jobCode;
    private String runStatus;
    private String message;
    private long syncedOrderCount;
    private long syncedPaymentOrderCount;
    private long syncedNotifyLogCount;
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

    public long getSyncedOrderCount() {
        return syncedOrderCount;
    }

    public void setSyncedOrderCount(long syncedOrderCount) {
        this.syncedOrderCount = syncedOrderCount;
    }

    public long getSyncedPaymentOrderCount() {
        return syncedPaymentOrderCount;
    }

    public void setSyncedPaymentOrderCount(long syncedPaymentOrderCount) {
        this.syncedPaymentOrderCount = syncedPaymentOrderCount;
    }

    public long getSyncedNotifyLogCount() {
        return syncedNotifyLogCount;
    }

    public void setSyncedNotifyLogCount(long syncedNotifyLogCount) {
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
}
