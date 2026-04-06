package com.bestwo.dataplatform.warehouse.entity;

import java.sql.Timestamp;

public class DwLineageRelationEntity {

    private String relationId;
    private String relationType;
    private String upstreamDatasourceCode;
    private String upstreamTableCode;
    private String downstreamDatasourceCode;
    private String downstreamTableCode;
    private String transformName;
    private Integer enabled;
    private Timestamp createdAt;
    private Timestamp updatedAt;

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
