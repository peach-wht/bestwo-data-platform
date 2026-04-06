package com.bestwo.dataplatform.warehouse.entity;

import java.sql.Timestamp;

public class DwMetaTableEntity {

    private String tableCode;
    private String datasourceCode;
    private String tableName;
    private String tableLayer;
    private String tableCategory;
    private String tableComment;
    private String owner;
    private String refreshStrategy;
    private Integer columnCount;
    private Timestamp createdAt;
    private Timestamp updatedAt;

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
