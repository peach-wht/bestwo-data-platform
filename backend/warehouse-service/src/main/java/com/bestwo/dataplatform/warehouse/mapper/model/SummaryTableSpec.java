package com.bestwo.dataplatform.warehouse.mapper.model;

public class SummaryTableSpec {

    private final String tableName;
    private final String statDateColumn;
    private final String orderCountExpression;
    private final String paidOrderCountExpression;
    private final String totalAmountExpression;
    private final String paidAmountExpression;
    private final String refundAmountExpression;

    public SummaryTableSpec(
        String tableName,
        String statDateColumn,
        String orderCountExpression,
        String paidOrderCountExpression,
        String totalAmountExpression,
        String paidAmountExpression,
        String refundAmountExpression
    ) {
        this.tableName = tableName;
        this.statDateColumn = statDateColumn;
        this.orderCountExpression = orderCountExpression;
        this.paidOrderCountExpression = paidOrderCountExpression;
        this.totalAmountExpression = totalAmountExpression;
        this.paidAmountExpression = paidAmountExpression;
        this.refundAmountExpression = refundAmountExpression;
    }

    public String getTableName() {
        return tableName;
    }

    public String getStatDateColumn() {
        return statDateColumn;
    }

    public String getOrderCountExpression() {
        return orderCountExpression;
    }

    public String getPaidOrderCountExpression() {
        return paidOrderCountExpression;
    }

    public String getTotalAmountExpression() {
        return totalAmountExpression;
    }

    public String getPaidAmountExpression() {
        return paidAmountExpression;
    }

    public String getRefundAmountExpression() {
        return refundAmountExpression;
    }
}
