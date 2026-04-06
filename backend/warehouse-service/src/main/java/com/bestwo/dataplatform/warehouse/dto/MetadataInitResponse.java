package com.bestwo.dataplatform.warehouse.dto;

import java.util.List;

public class MetadataInitResponse {

    private String database;
    private Integer datasourceCount;
    private Integer tableCount;
    private Integer columnCount;
    private List<String> executedResources;

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public Integer getDatasourceCount() {
        return datasourceCount;
    }

    public void setDatasourceCount(Integer datasourceCount) {
        this.datasourceCount = datasourceCount;
    }

    public Integer getTableCount() {
        return tableCount;
    }

    public void setTableCount(Integer tableCount) {
        this.tableCount = tableCount;
    }

    public Integer getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(Integer columnCount) {
        this.columnCount = columnCount;
    }

    public List<String> getExecutedResources() {
        return executedResources;
    }

    public void setExecutedResources(List<String> executedResources) {
        this.executedResources = executedResources;
    }
}
