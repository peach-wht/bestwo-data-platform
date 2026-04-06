CREATE TABLE IF NOT EXISTS dw_meta_table (
    table_code VARCHAR(128) NOT NULL,
    datasource_code VARCHAR(64) NOT NULL,
    table_name VARCHAR(128) NOT NULL,
    table_layer VARCHAR(32) NOT NULL,
    table_category VARCHAR(32) NULL,
    table_comment VARCHAR(255) NULL,
    owner VARCHAR(64) NULL,
    refresh_strategy VARCHAR(32) NULL,
    column_count INT NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL
)
UNIQUE KEY(table_code)
DISTRIBUTED BY HASH(table_code) BUCKETS 4
PROPERTIES (
    "replication_num" = "1"
);
