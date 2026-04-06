CREATE TABLE IF NOT EXISTS dw_sync_job (
    job_code VARCHAR(64) NOT NULL,
    job_name VARCHAR(128) NOT NULL,
    source_type VARCHAR(32) NOT NULL,
    source_tables STRING NULL,
    target_tables STRING NULL,
    enabled TINYINT NOT NULL,
    remark VARCHAR(255) NULL,
    last_run_status VARCHAR(32) NULL,
    last_run_message VARCHAR(255) NULL,
    last_run_at DATETIME NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL
)
UNIQUE KEY(job_code)
DISTRIBUTED BY HASH(job_code) BUCKETS 4
PROPERTIES (
    "replication_num" = "1"
);
