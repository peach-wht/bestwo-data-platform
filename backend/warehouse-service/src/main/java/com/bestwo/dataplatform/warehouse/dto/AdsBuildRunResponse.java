package com.bestwo.dataplatform.warehouse.dto;

import java.time.Instant;

public class AdsBuildRunResponse {

    private String logId;
    private String jobCode;
    private String runStatus;
    private String message;
    private Long dwsRowCount;
    private Long adsOrderDaySummaryRowCount;
    private Long adsDashboardOverviewRowCount;
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

    public Long getDwsRowCount() {
        return dwsRowCount;
    }

    public void setDwsRowCount(Long dwsRowCount) {
        this.dwsRowCount = dwsRowCount;
    }

    public Long getAdsOrderDaySummaryRowCount() {
        return adsOrderDaySummaryRowCount;
    }

    public void setAdsOrderDaySummaryRowCount(Long adsOrderDaySummaryRowCount) {
        this.adsOrderDaySummaryRowCount = adsOrderDaySummaryRowCount;
    }

    public Long getAdsDashboardOverviewRowCount() {
        return adsDashboardOverviewRowCount;
    }

    public void setAdsDashboardOverviewRowCount(Long adsDashboardOverviewRowCount) {
        this.adsDashboardOverviewRowCount = adsDashboardOverviewRowCount;
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
