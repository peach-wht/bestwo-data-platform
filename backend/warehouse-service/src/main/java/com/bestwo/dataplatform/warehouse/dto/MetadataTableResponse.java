package com.bestwo.dataplatform.warehouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MetadataTableResponse {

    @JsonProperty("table_code")
    private String tableCode;

    @JsonProperty("datasource_code")
    private String datasourceCode;

    @JsonProperty("table_name")
    private String tableName;

    @JsonProperty("table_layer")
    private String tableLayer;

    @JsonProperty("table_category")
    private String tableCategory;

    @JsonProperty("table_comment")
    private String tableComment;

    @JsonProperty("owner")
    private String owner;

    @JsonProperty("refresh_strategy")
    private String refreshStrategy;

    @JsonProperty("column_count")
    private Integer columnCount;

    public String getTableCode() {
        return tableCode;
    }

    public void setTableCode(String tableCode) {
        this.tableCode = tableCode;
    }

    public String getDatasourceCode() {
        return datasourceCode;
    }

    public void setDatasourceCode(String datasourceCode) {
        this.datasourceCode = datasourceCode;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableLayer() {
        return tableLayer;
    }

    public void setTableLayer(String tableLayer) {
        this.tableLayer = tableLayer;
    }

    public String getTableCategory() {
        return tableCategory;
    }

    public void setTableCategory(String tableCategory) {
        this.tableCategory = tableCategory;
    }

    public String getTableComment() {
        return tableComment;
    }

    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRefreshStrategy() {
        return refreshStrategy;
    }

    public void setRefreshStrategy(String refreshStrategy) {
        this.refreshStrategy = refreshStrategy;
    }

    public Integer getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(Integer columnCount) {
        this.columnCount = columnCount;
    }
}
