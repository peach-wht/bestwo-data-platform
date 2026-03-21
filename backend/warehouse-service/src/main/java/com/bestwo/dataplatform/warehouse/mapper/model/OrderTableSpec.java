package com.bestwo.dataplatform.warehouse.mapper.model;

public class OrderTableSpec {

    private final String tableName;
    private final String orderNoColumn;
    private final String orderTimeColumn;
    private final String orderIdSelect;
    private final String orderNoSelect;
    private final String orderStatusSelect;
    private final String payStatusSelect;
    private final String orderTimeSelect;
    private final String payTimeSelect;
    private final String totalAmountSelect;
    private final String buyerNicknameSelect;

    public OrderTableSpec(
        String tableName,
        String orderNoColumn,
        String orderTimeColumn,
        String orderIdSelect,
        String orderNoSelect,
        String orderStatusSelect,
        String payStatusSelect,
        String orderTimeSelect,
        String payTimeSelect,
        String totalAmountSelect,
        String buyerNicknameSelect
    ) {
        this.tableName = tableName;
        this.orderNoColumn = orderNoColumn;
        this.orderTimeColumn = orderTimeColumn;
        this.orderIdSelect = orderIdSelect;
        this.orderNoSelect = orderNoSelect;
        this.orderStatusSelect = orderStatusSelect;
        this.payStatusSelect = payStatusSelect;
        this.orderTimeSelect = orderTimeSelect;
        this.payTimeSelect = payTimeSelect;
        this.totalAmountSelect = totalAmountSelect;
        this.buyerNicknameSelect = buyerNicknameSelect;
    }

    public String getTableName() {
        return tableName;
    }

    public String getOrderNoColumn() {
        return orderNoColumn;
    }

    public String getOrderTimeColumn() {
        return orderTimeColumn;
    }

    public String getOrderIdSelect() {
        return orderIdSelect;
    }

    public String getOrderNoSelect() {
        return orderNoSelect;
    }

    public String getOrderStatusSelect() {
        return orderStatusSelect;
    }

    public String getPayStatusSelect() {
        return payStatusSelect;
    }

    public String getOrderTimeSelect() {
        return orderTimeSelect;
    }

    public String getPayTimeSelect() {
        return payTimeSelect;
    }

    public String getTotalAmountSelect() {
        return totalAmountSelect;
    }

    public String getBuyerNicknameSelect() {
        return buyerNicknameSelect;
    }
}
