CREATE TABLE IF NOT EXISTS ods_wx_payment_notify (
    notify_log_id VARCHAR(64) NOT NULL,
    notify_id VARCHAR(128) NULL,
    platform VARCHAR(32) NULL,
    notify_type VARCHAR(32) NULL,
    payment_order_id VARCHAR(64) NULL,
    payment_order_no VARCHAR(64) NULL,
    order_id VARCHAR(64) NULL,
    order_no VARCHAR(64) NULL,
    channel_order_no VARCHAR(128) NULL,
    event_type VARCHAR(64) NULL,
    event_status VARCHAR(64) NULL,
    summary VARCHAR(255) NULL,
    request_headers_json STRING NULL,
    request_body STRING NULL,
    encrypt_type VARCHAR(32) NULL,
    resource_ciphertext STRING NULL,
    resource_nonce VARCHAR(128) NULL,
    resource_associated_data VARCHAR(255) NULL,
    signature_serial_no VARCHAR(128) NULL,
    signature_value STRING NULL,
    process_status VARCHAR(32) NULL,
    process_message VARCHAR(255) NULL,
    processed_at DATETIME NULL,
    received_at DATETIME NULL,
    ext_json STRING NULL,
    sync_time DATETIME NULL
)
UNIQUE KEY(notify_log_id)
DISTRIBUTED BY HASH(notify_log_id) BUCKETS 8
PROPERTIES (
    "replication_num" = "1"
);
