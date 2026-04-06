CREATE TABLE IF NOT EXISTS dw_alert_record (
    alert_id VARCHAR(64) NOT NULL,
    alert_type VARCHAR(32) NOT NULL,
    alert_level VARCHAR(32) NOT NULL,
    alert_source VARCHAR(64) NOT NULL,
    source_code VARCHAR(128) NULL,
    source_name VARCHAR(128) NULL,
    alert_status VARCHAR(32) NOT NULL,
    alert_title VARCHAR(128) NOT NULL,
    alert_message VARCHAR(255) NULL,
    fired_at DATETIME NULL,
    resolved_at DATETIME NULL,
    created_at DATETIME NULL
)
UNIQUE KEY(alert_id)
DISTRIBUTED BY HASH(alert_id) BUCKETS 4
PROPERTIES (
    "replication_num" = "1"
);
