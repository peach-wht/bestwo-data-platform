CREATE TABLE IF NOT EXISTS dw_quality_result (
    result_id VARCHAR(64) NOT NULL,
    rule_code VARCHAR(64) NOT NULL,
    rule_name VARCHAR(128) NOT NULL,
    table_name VARCHAR(128) NOT NULL,
    result_status VARCHAR(32) NOT NULL,
    result_level VARCHAR(32) NOT NULL,
    failed_count BIGINT NULL,
    total_count BIGINT NULL,
    pass_count BIGINT NULL,
    threshold_value BIGINT NULL,
    message VARCHAR(255) NULL,
    checked_at DATETIME NULL,
    created_at DATETIME NULL
)
UNIQUE KEY(result_id)
DISTRIBUTED BY HASH(result_id) BUCKETS 4
PROPERTIES (
    "replication_num" = "1"
);
