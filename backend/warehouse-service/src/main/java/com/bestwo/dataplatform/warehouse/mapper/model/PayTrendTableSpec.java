package com.bestwo.dataplatform.warehouse.mapper.model;

public class PayTrendTableSpec {

    private final String tableName;
    private final String statDateColumn;
    private final String orderCountExpression;
    private final String paidOrderCountExpression;
    private final String unpaidOrderCountExpression;
    private final String closedOrderCountExpression;
    private final String totalAmountExpression;
    private final String paidAmountExpression;
    private final String refundAmountExpression;
    private final String paySuccessRateExpression;

    public PayTrendTableSpec(
        String tableName,
        String statDateColumn,
        String orderCountExpression,
        String paidOrderCountExpression,
        String unpaidOrderCountExpression,
        String closedOrderCountExpression,
        String totalAmountExpression,
        String paidAmountExpression,
        String refundAmountExpression,
        String paySuccessRateExpression
    ) {
        this.tableName = tableName;
        this.statDateColumn = statDateColumn;
        this.orderCountExpression = orderCountExpression;
        this.paidOrderCountExpression = paidOrderCountExpression;
        this.unpaidOrderCountExpression = unpaidOrderCountExpression;
        this.closedOrderCountExpression = closedOrderCountExpression;
        this.totalAmountExpression = totalAmountExpression;
        this.paidAmountExpression = paidAmountExpression;
        this.refundAmountExpression = refundAmountExpression;
        this.paySuccessRateExpression = paySuccessRateExpression;
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

    public String getUnpaidOrderCountExpression() {
        return unpaidOrderCountExpression;
    }

    public String getClosedOrderCountExpression() {
        return closedOrderCountExpression;
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

    public String getPaySuccessRateExpression() {
        return paySuccessRateExpression;
    }
}
