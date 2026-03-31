-- PostgreSQL DDL for payment notify raw log table.
-- Raw callback bodies are kept for audit, retry and idempotency investigation.

CREATE TABLE IF NOT EXISTS biz_payment_notify_log (
    id BIGSERIAL PRIMARY KEY,
    notify_log_id VARCHAR(64) NOT NULL,
    notify_id VARCHAR(128),
    platform VARCHAR(32) NOT NULL,
    notify_type VARCHAR(32) NOT NULL DEFAULT 'PAY',
    payment_order_id VARCHAR(64),
    payment_order_no VARCHAR(64),
    order_id VARCHAR(64),
    order_no VARCHAR(64),
    channel_order_no VARCHAR(64),
    event_type VARCHAR(64),
    event_status VARCHAR(64),
    summary VARCHAR(255),
    request_headers_json JSONB,
    request_body TEXT NOT NULL,
    encrypt_type VARCHAR(32),
    resource_ciphertext TEXT,
    resource_nonce VARCHAR(128),
    resource_associated_data VARCHAR(255),
    signature_serial_no VARCHAR(128),
    signature_value VARCHAR(255),
    process_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    process_message VARCHAR(255),
    processed_at TIMESTAMPTZ,
    received_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ext_json JSONB,
    CONSTRAINT uk_biz_payment_notify_log_id UNIQUE (notify_log_id),
    CONSTRAINT uk_biz_payment_notify_platform_notify_id UNIQUE (platform, notify_id),
    CONSTRAINT fk_biz_payment_notify_payment_order_id FOREIGN KEY (payment_order_id)
        REFERENCES biz_payment_order(payment_order_id),
    CONSTRAINT fk_biz_payment_notify_order_id FOREIGN KEY (order_id)
        REFERENCES biz_order(order_id)
);

CREATE INDEX IF NOT EXISTS idx_biz_payment_notify_payment_order_no ON biz_payment_notify_log (payment_order_no);
CREATE INDEX IF NOT EXISTS idx_biz_payment_notify_order_no ON biz_payment_notify_log (order_no);
CREATE INDEX IF NOT EXISTS idx_biz_payment_notify_process_status ON biz_payment_notify_log (process_status);
CREATE INDEX IF NOT EXISTS idx_biz_payment_notify_received_at ON biz_payment_notify_log (received_at DESC);
CREATE INDEX IF NOT EXISTS idx_biz_payment_notify_channel_order_no ON biz_payment_notify_log (channel_order_no);

COMMENT ON TABLE biz_payment_notify_log IS '支付回调日志表';
COMMENT ON COLUMN biz_payment_notify_log.id IS '数据库自增主键';
COMMENT ON COLUMN biz_payment_notify_log.notify_log_id IS '系统内部稳定通知日志ID';
COMMENT ON COLUMN biz_payment_notify_log.notify_id IS '渠道通知ID';
COMMENT ON COLUMN biz_payment_notify_log.platform IS '支付平台，如 WECHAT_PAY';
COMMENT ON COLUMN biz_payment_notify_log.notify_type IS '通知类型，如 PAY/REFUND';
COMMENT ON COLUMN biz_payment_notify_log.payment_order_id IS '关联支付单ID';
COMMENT ON COLUMN biz_payment_notify_log.payment_order_no IS '关联支付单号';
COMMENT ON COLUMN biz_payment_notify_log.order_id IS '关联业务订单ID';
COMMENT ON COLUMN biz_payment_notify_log.order_no IS '关联业务订单号';
COMMENT ON COLUMN biz_payment_notify_log.channel_order_no IS '渠道订单号';
COMMENT ON COLUMN biz_payment_notify_log.event_type IS '渠道事件类型';
COMMENT ON COLUMN biz_payment_notify_log.event_status IS '渠道事件状态';
COMMENT ON COLUMN biz_payment_notify_log.summary IS '通知摘要';
COMMENT ON COLUMN biz_payment_notify_log.request_headers_json IS '回调请求头JSON';
COMMENT ON COLUMN biz_payment_notify_log.request_body IS '回调原始请求体';
COMMENT ON COLUMN biz_payment_notify_log.encrypt_type IS '加密类型';
COMMENT ON COLUMN biz_payment_notify_log.resource_ciphertext IS '加密资源密文';
COMMENT ON COLUMN biz_payment_notify_log.resource_nonce IS '加密资源nonce';
COMMENT ON COLUMN biz_payment_notify_log.resource_associated_data IS '加密资源associated_data';
COMMENT ON COLUMN biz_payment_notify_log.signature_serial_no IS '证书序列号';
COMMENT ON COLUMN biz_payment_notify_log.signature_value IS '签名值';
COMMENT ON COLUMN biz_payment_notify_log.process_status IS '通知处理状态，如 PENDING/PROCESSED/FAILED';
COMMENT ON COLUMN biz_payment_notify_log.process_message IS '通知处理结果描述';
COMMENT ON COLUMN biz_payment_notify_log.processed_at IS '处理完成时间';
COMMENT ON COLUMN biz_payment_notify_log.received_at IS '接收时间';
COMMENT ON COLUMN biz_payment_notify_log.ext_json IS '扩展字段JSON';
