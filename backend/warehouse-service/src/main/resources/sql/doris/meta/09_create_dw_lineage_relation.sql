CREATE TABLE IF NOT EXISTS dw_lineage_relation (
    relation_id VARCHAR(128) NOT NULL,
    relation_type VARCHAR(32) NOT NULL,
    upstream_datasource_code VARCHAR(64) NOT NULL,
    upstream_table_code VARCHAR(128) NOT NULL,
    downstream_datasource_code VARCHAR(64) NOT NULL,
    downstream_table_code VARCHAR(128) NOT NULL,
    transform_name VARCHAR(128) NULL,
    enabled TINYINT NOT NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL
)
UNIQUE KEY(relation_id)
DISTRIBUTED BY HASH(relation_id) BUCKETS 4
PROPERTIES (
    "replication_num" = "1"
);
