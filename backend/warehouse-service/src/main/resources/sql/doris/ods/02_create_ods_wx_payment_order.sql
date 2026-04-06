CREATE TABLE IF NOT EXISTS ods_wx_payment_order (
    payment_order_id VARCHAR(64) NOT NULL,
    payment_order_no VARCHAR(64) NOT NULL,
    order_id VARCHAR(64) NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    platform VARCHAR(32) NULL,
    trade_type VARCHAR(32) NULL,
    merchant_code VARCHAR(64) NULL,
    channel_app_id VARCHAR(64) NULL,
    channel_merchant_id VARCHAR(64) NULL,
    subject VARCHAR(128) NULL,
    description VARCHAR(255) NULL,
    currency VARCHAR(16) NULL,
    request_amount_fen BIGINT NULL,
    success_amount_fen BIGINT NULL,
    payer_id VARCHAR(128) NULL,
    client_ip VARCHAR(64) NULL,
    notify_url VARCHAR(255) NULL,
    channel_order_no VARCHAR(128) NULL,
    channel_prepay_id VARCHAR(128) NULL,
    code_url STRING NULL,
    status VARCHAR(32) NULL,
    fail_code VARCHAR(64) NULL,
    fail_message VARCHAR(255) NULL,
    expired_time DATETIME NULL,
    success_time DATETIME NULL,
    closed_time DATETIME NULL,
    last_notify_time DATETIME NULL,
    ext_json STRING NULL,
    version INT NULL,
    created_by VARCHAR(64) NULL,
    updated_by VARCHAR(64) NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    sync_time DATETIME NULL
)
UNIQUE KEY(payment_order_id)
DISTRIBUTED BY HASH(payment_order_id) BUCKETS 8
PROPERTIES (
    "replication_num" = "1"
);
