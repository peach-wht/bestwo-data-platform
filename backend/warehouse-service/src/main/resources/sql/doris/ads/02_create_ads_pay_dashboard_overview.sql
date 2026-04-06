CREATE TABLE IF NOT EXISTS ads_pay_dashboard_overview (
    metric_scope VARCHAR(32) NOT NULL,
    latest_stat_date DATE NULL,
    order_count BIGINT NULL,
    paid_order_count BIGINT NULL,
    unpaid_order_count BIGINT NULL,
    total_amount DECIMAL(18, 2) NULL,
    paid_amount DECIMAL(18, 2) NULL,
    refund_amount DECIMAL(18, 2) NULL,
    pay_success_rate DECIMAL(18, 4) NULL,
    sync_time DATETIME NULL
)
UNIQUE KEY(metric_scope)
DISTRIBUTED BY HASH(metric_scope) BUCKETS 2
PROPERTIES (
    "replication_num" = "1"
);
