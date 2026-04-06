CREATE TABLE IF NOT EXISTS dw_meta_column (
    table_code VARCHAR(128) NOT NULL,
    column_name VARCHAR(128) NOT NULL,
    data_type VARCHAR(128) NULL,
    nullable_flag TINYINT NULL,
    column_comment VARCHAR(255) NULL,
    column_order INT NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL
)
UNIQUE KEY(table_code, column_name)
DISTRIBUTED BY HASH(table_code) BUCKETS 4
PROPERTIES (
    "replication_num" = "1"
);
