package com.bestwo.dataplatform.warehouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QualityResultResponse {

    @JsonProperty("rule_code")
    private String ruleCode;

    @JsonProperty("rule_name")
    private String ruleName;

    @JsonProperty("table_name")
    private String tableName;

    @JsonProperty("result_status")
    private String resultStatus;

    @JsonProperty("result_level")
    private String resultLevel;

    @JsonProperty("failed_count")
    private Long failedCount;

    @JsonProperty("total_count")
    private Long totalCount;

    @JsonProperty("message")
    private String message;

    @JsonProperty("checked_at")
    private String checkedAt;

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getResultLevel() {
        return resultLevel;
    }

    public void setResultLevel(String resultLevel) {
        this.resultLevel = resultLevel;
    }

    public Long getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Long failedCount) {
        this.failedCount = failedCount;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(String checkedAt) {
        this.checkedAt = checkedAt;
    }
}
