package com.bestwo.dataplatform.warehouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LineageRelationResponse {

    @JsonProperty("relation_id")
    private String relationId;

    @JsonProperty("relation_type")
    private String relationType;

    @JsonProperty("upstream_datasource_code")
    private String upstreamDatasourceCode;

    @JsonProperty("upstream_table_code")
    private String upstreamTableCode;

    @JsonProperty("downstream_datasource_code")
    private String downstreamDatasourceCode;

    @JsonProperty("downstream_table_code")
    private String downstreamTableCode;

    @JsonProperty("transform_name")
    private String transformName;

    @JsonProperty("enabled")
    private Integer enabled;

    public String getRelationId() {
        return relationId;
    }

    public void setRelationId(String relationId) {
        this.relationId = relationId;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public String getUpstreamDatasourceCode() {
        return upstreamDatasourceCode;
    }

    public void setUpstreamDatasourceCode(String upstreamDatasourceCode) {
        this.upstreamDatasourceCode = upstreamDatasourceCode;
    }

    public String getUpstreamTableCode() {
        return upstreamTableCode;
    }

    public void setUpstreamTableCode(String upstreamTableCode) {
        this.upstreamTableCode = upstreamTableCode;
    }

    public String getDownstreamDatasourceCode() {
        return downstreamDatasourceCode;
    }

    public void setDownstreamDatasourceCode(String downstreamDatasourceCode) {
        this.downstreamDatasourceCode = downstreamDatasourceCode;
    }

    public String getDownstreamTableCode() {
        return downstreamTableCode;
    }

    public void setDownstreamTableCode(String downstreamTableCode) {
        this.downstreamTableCode = downstreamTableCode;
    }

    public String getTransformName() {
        return transformName;
    }

    public void setTransformName(String transformName) {
        this.transformName = transformName;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }
}
