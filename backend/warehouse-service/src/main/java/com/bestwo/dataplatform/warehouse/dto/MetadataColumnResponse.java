package com.bestwo.dataplatform.warehouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MetadataColumnResponse {

    @JsonProperty("table_code")
    private String tableCode;

    @JsonProperty("column_name")
    private String columnName;

    @JsonProperty("data_type")
    private String dataType;

    @JsonProperty("nullable_flag")
    private Integer nullableFlag;

    @JsonProperty("column_comment")
    private String columnComment;

    @JsonProperty("column_order")
    private Integer columnOrder;

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
}
