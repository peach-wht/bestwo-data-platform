package com.bestwo.dataplatform.warehouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AlertRecordResponse {

    @JsonProperty("alert_id")
    private String alertId;

    @JsonProperty("alert_type")
    private String alertType;

    @JsonProperty("alert_level")
    private String alertLevel;

    @JsonProperty("alert_source")
    private String alertSource;

    @JsonProperty("source_code")
    private String sourceCode;

    @JsonProperty("source_name")
    private String sourceName;

    @JsonProperty("alert_status")
    private String alertStatus;

    @JsonProperty("alert_title")
    private String alertTitle;

    @JsonProperty("alert_message")
    private String alertMessage;

    @JsonProperty("fired_at")
    private String firedAt;

    @JsonProperty("resolved_at")
    private String resolvedAt;

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }

    public String getAlertSource() {
        return alertSource;
    }

    public void setAlertSource(String alertSource) {
        this.alertSource = alertSource;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getAlertStatus() {
        return alertStatus;
    }

    public void setAlertStatus(String alertStatus) {
        this.alertStatus = alertStatus;
    }

    public String getAlertTitle() {
        return alertTitle;
    }

    public void setAlertTitle(String alertTitle) {
        this.alertTitle = alertTitle;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public String getFiredAt() {
        return firedAt;
    }

    public void setFiredAt(String firedAt) {
        this.firedAt = firedAt;
    }

    public String getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(String resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
