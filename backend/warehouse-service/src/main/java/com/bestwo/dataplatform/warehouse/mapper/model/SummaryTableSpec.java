package com.bestwo.dataplatform.warehouse.mapper.model;

public class SummaryTableSpec {

    private final String tableName;
    private final String statDateColumn;
    private final String statDateSelect;
    private final String orderCountSelect;
    private final String paidOrderCountSelect;
    private final String totalAmountSelect;
    private final String paidAmountSelect;
    private final String refundAmountSelect;

    public SummaryTableSpec(
        String tableName,
        String statDateColumn,
        String statDateSelect,
        String orderCountSelect,
        String paidOrderCountSelect,
        String totalAmountSelect,
        String paidAmountSelect,
        String refundAmountSelect
    ) {
        this.tableName = tableName;
        this.statDateColumn = statDateColumn;
        this.statDateSelect = statDateSelect;
        this.orderCountSelect = orderCountSelect;
        this.paidOrderCountSelect = paidOrderCountSelect;
        this.totalAmountSelect = totalAmountSelect;
        this.paidAmountSelect = paidAmountSelect;
        this.refundAmountSelect = refundAmountSelect;
    }

    public String getTableName() {
        return tableName;
    }

    public String getStatDateColumn() {
        return statDateColumn;
    }

    public String getStatDateSelect() {
        return statDateSelect;
    }

    public String getOrderCountSelect() {
        return orderCountSelect;
    }

    public String getPaidOrderCountSelect() {
        return paidOrderCountSelect;
    }

    public String getTotalAmountSelect() {
        return totalAmountSelect;
    }

    public String getPaidAmountSelect() {
        return paidAmountSelect;
    }

    public String getRefundAmountSelect() {
        return refundAmountSelect;
    }
}
