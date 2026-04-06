package com.bestwo.dataplatform.warehouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MetadataDatasourceResponse {

    @JsonProperty("datasource_code")
    private String datasourceCode;

    @JsonProperty("datasource_name")
    private String datasourceName;

    @JsonProperty("datasource_type")
    private String datasourceType;

    @JsonProperty("host")
    private String host;

    @JsonProperty("port")
    private Integer port;

    @JsonProperty("database_name")
    private String databaseName;

    @JsonProperty("owner")
    private String owner;

    @JsonProperty("description")
    private String description;

    @JsonProperty("enabled")
    private Integer enabled;

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
}
