CREATE TABLE IF NOT EXISTS dw_sync_job_log (
    log_id VARCHAR(64) NOT NULL,
    job_code VARCHAR(64) NOT NULL,
    job_name VARCHAR(128) NOT NULL,
    run_status VARCHAR(32) NOT NULL,
    message VARCHAR(255) NULL,
    synced_order_count BIGINT NULL,
    synced_payment_order_count BIGINT NULL,
    synced_notify_log_count BIGINT NULL,
    started_at DATETIME NULL,
    finished_at DATETIME NULL,
    duration_ms BIGINT NULL,
    created_at DATETIME NULL
)
UNIQUE KEY(log_id)
DISTRIBUTED BY HASH(log_id) BUCKETS 4
PROPERTIES (
    "replication_num" = "1"
);
