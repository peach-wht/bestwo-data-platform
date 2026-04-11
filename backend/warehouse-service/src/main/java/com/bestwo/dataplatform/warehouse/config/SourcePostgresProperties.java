package com.bestwo.dataplatform.warehouse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "source-db")
public class SourcePostgresProperties {

    private String host;
    private Integer port;
    private String database;
    private String schema;
    private String username;
    private String password;

    public String getJdbcUrl() {
        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        if (schema == null || schema.isBlank()) {
            return jdbcUrl;
        }
        return jdbcUrl + "?currentSchema=" + schema;
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

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUsername() {
        return username;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
