CREATE TABLE IF NOT EXISTS ods_wx_order (
    order_id VARCHAR(64) NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    external_order_no VARCHAR(64) NULL,
    biz_type VARCHAR(32) NULL,
    order_source VARCHAR(32) NULL,
    order_title VARCHAR(128) NULL,
    order_description VARCHAR(255) NULL,
    buyer_id VARCHAR(64) NULL,
    buyer_nickname VARCHAR(128) NULL,
    currency VARCHAR(16) NULL,
    total_amount_fen BIGINT NULL,
    payable_amount_fen BIGINT NULL,
    paid_amount_fen BIGINT NULL,
    refunded_amount_fen BIGINT NULL,
    order_status VARCHAR(32) NULL,
    pay_status VARCHAR(32) NULL,
    preferred_pay_platform VARCHAR(32) NULL,
    preferred_trade_type VARCHAR(32) NULL,
    success_pay_platform VARCHAR(32) NULL,
    latest_payment_order_no VARCHAR(64) NULL,
    latest_channel_order_no VARCHAR(128) NULL,
    paid_time DATETIME NULL,
    expired_time DATETIME NULL,
    closed_time DATETIME NULL,
    cancelled_time DATETIME NULL,
    remark VARCHAR(255) NULL,
    ext_json STRING NULL,
    version INT NULL,
    created_by VARCHAR(64) NULL,
    updated_by VARCHAR(64) NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    sync_time DATETIME NULL
)
UNIQUE KEY(order_id)
DISTRIBUTED BY HASH(order_id) BUCKETS 8
PROPERTIES (
    "replication_num" = "1"
);
