CREATE TABLE IF NOT EXISTS dw_meta_datasource (
    datasource_code VARCHAR(64) NOT NULL,
    datasource_name VARCHAR(128) NOT NULL,
    datasource_type VARCHAR(32) NOT NULL,
    host VARCHAR(128) NULL,
    port INT NULL,
    database_name VARCHAR(128) NULL,
    owner VARCHAR(64) NULL,
    description VARCHAR(255) NULL,
    enabled TINYINT NOT NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL
)
UNIQUE KEY(datasource_code)
DISTRIBUTED BY HASH(datasource_code) BUCKETS 4
PROPERTIES (
    "replication_num" = "1"
);
