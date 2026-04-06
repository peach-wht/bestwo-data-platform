package com.bestwo.dataplatform.warehouse.entity;

import java.sql.Timestamp;

public class DwMetaColumnEntity {

    private String tableCode;
    private String columnName;
    private String dataType;
    private Integer nullableFlag;
    private String columnComment;
    private Integer columnOrder;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public String getTableCode() {
        return tableCode;
    }

    public void setTableCode(String tableCode) {
        this.tableCode = tableCode;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Integer getNullableFlag() {
        return nullableFlag;
    }

    public void setNullableFlag(Integer nullableFlag) {
        this.nullableFlag = nullableFlag;
    }

    public String getColumnComment() {
        return columnComment;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }

    public Integer getColumnOrder() {
        return columnOrder;
    }

    public void setColumnOrder(Integer columnOrder) {
        this.columnOrder = columnOrder;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
