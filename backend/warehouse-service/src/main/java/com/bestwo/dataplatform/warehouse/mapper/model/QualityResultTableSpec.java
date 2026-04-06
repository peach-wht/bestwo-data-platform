package com.bestwo.dataplatform.warehouse.mapper.model;

public class QualityResultTableSpec {

    private final String tableName;
    private final String ruleCodeExpression;
    private final String ruleNameExpression;
    private final String tableNameExpression;
    private final String resultStatusExpression;
    private final String resultLevelExpression;
    private final String failedCountExpression;
    private final String totalCountExpression;
    private final String messageExpression;
    private final String checkedAtExpression;
    private final String checkedAtOrderExpression;

    public QualityResultTableSpec(
        String tableName,
        String ruleCodeExpression,
        String ruleNameExpression,
        String tableNameExpression,
        String resultStatusExpression,
        String resultLevelExpression,
        String failedCountExpression,
        String totalCountExpression,
        String messageExpression,
        String checkedAtExpression,
        String checkedAtOrderExpression
    ) {
        this.tableName = tableName;
        this.ruleCodeExpression = ruleCodeExpression;
        this.ruleNameExpression = ruleNameExpression;
        this.tableNameExpression = tableNameExpression;
        this.resultStatusExpression = resultStatusExpression;
        this.resultLevelExpression = resultLevelExpression;
        this.failedCountExpression = failedCountExpression;
        this.totalCountExpression = totalCountExpression;
        this.messageExpression = messageExpression;
        this.checkedAtExpression = checkedAtExpression;
        this.checkedAtOrderExpression = checkedAtOrderExpression;
    }

    public String getTableName() {
        return tableName;
    }

    public String getRuleCodeExpression() {
        return ruleCodeExpression;
    }

    public String getRuleNameExpression() {
        return ruleNameExpression;
    }

    public String getTableNameExpression() {
        return tableNameExpression;
    }

    public String getResultStatusExpression() {
        return resultStatusExpression;
    }

    public String getResultLevelExpression() {
        return resultLevelExpression;
    }

    public String getFailedCountExpression() {
        return failedCountExpression;
    }

    public String getTotalCountExpression() {
        return totalCountExpression;
    }

    public String getMessageExpression() {
        return messageExpression;
    }

    public String getCheckedAtExpression() {
        return checkedAtExpression;
    }

    public String getCheckedAtOrderExpression() {
        return checkedAtOrderExpression;
    }
}
