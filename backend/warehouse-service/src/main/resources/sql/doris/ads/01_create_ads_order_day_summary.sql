CREATE TABLE IF NOT EXISTS ads_order_day_summary (
    stat_date DATE NOT NULL,
    order_count BIGINT NULL,
    paid_order_count BIGINT NULL,
    total_amount DECIMAL(18, 2) NULL,
    paid_amount DECIMAL(18, 2) NULL,
    refund_amount DECIMAL(18, 2) NULL,
    sync_time DATETIME NULL
)
UNIQUE KEY(stat_date)
DISTRIBUTED BY HASH(stat_date) BUCKETS 4
PROPERTIES (
    "replication_num" = "1"
);
