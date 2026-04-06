CREATE TABLE IF NOT EXISTS dw_quality_rule (
    rule_code VARCHAR(64) NOT NULL,
    rule_name VARCHAR(128) NOT NULL,
    table_name VARCHAR(128) NOT NULL,
    rule_level VARCHAR(32) NOT NULL,
    rule_type VARCHAR(32) NOT NULL,
    rule_sql STRING NOT NULL,
    total_sql STRING NULL,
    threshold_value BIGINT NULL,
    enabled TINYINT NOT NULL,
    rule_order INT NULL,
    description VARCHAR(255) NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL
)
UNIQUE KEY(rule_code)
DISTRIBUTED BY HASH(rule_code) BUCKETS 4
PROPERTIES (
    "replication_num" = "1"
);
