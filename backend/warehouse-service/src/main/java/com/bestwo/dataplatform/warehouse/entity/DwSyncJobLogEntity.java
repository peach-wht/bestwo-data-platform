package com.bestwo.dataplatform.warehouse.entity;

import java.sql.Timestamp;

public class DwSyncJobLogEntity {

    private String logId;
    private String jobCode;
    private String jobName;
    private String runStatus;
    private String message;
    private Long syncedOrderCount;
    private Long syncedPaymentOrderCount;
    private Long syncedNotifyLogCount;
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
