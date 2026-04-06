CREATE TABLE IF NOT EXISTS dw_job_log (
    log_id VARCHAR(64) NOT NULL,
    job_code VARCHAR(64) NOT NULL,
    job_name VARCHAR(128) NOT NULL,
    job_type VARCHAR(32) NOT NULL,
    source_type VARCHAR(32) NULL,
    source_tables STRING NULL,
    target_tables STRING NULL,
    run_status VARCHAR(32) NOT NULL,
    message VARCHAR(255) NULL,
    metric_one_label VARCHAR(64) NULL,
    metric_one_value BIGINT NULL,
    metric_two_label VARCHAR(64) NULL,
    metric_two_value BIGINT NULL,
    metric_three_label VARCHAR(64) NULL,
    metric_three_value BIGINT NULL,
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
