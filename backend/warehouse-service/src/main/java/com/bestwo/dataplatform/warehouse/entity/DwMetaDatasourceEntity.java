package com.bestwo.dataplatform.warehouse.entity;

import java.sql.Timestamp;

public class DwMetaDatasourceEntity {

    private String datasourceCode;
    private String datasourceName;
    private String datasourceType;
    private String host;
    private Integer port;
    private String databaseName;
    private String owner;
    private String description;
    private Integer enabled;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public String getDatasourceCode() {
        return datasourceCode;
    }

    public void setDatasourceCode(String datasourceCode) {
        this.datasourceCode = datasourceCode;
    }

    public String getDatasourceName() {
        return datasourceName;
    }

    public void setDatasourceName(String datasourceName) {
        this.datasourceName = datasourceName;
    }

    public String getDatasourceType() {
        return datasourceType;
    }

    public void setDatasourceType(String datasourceType) {
        this.datasourceType = datasourceType;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
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
