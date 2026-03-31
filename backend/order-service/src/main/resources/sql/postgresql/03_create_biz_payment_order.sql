-- PostgreSQL DDL for business payment order table.
-- One business order can have multiple payment attempts, so payment order is
-- split from biz_order and keeps platform-specific channel details.

CREATE TABLE IF NOT EXISTS biz_payment_order (
    id BIGSERIAL PRIMARY KEY,
    payment_order_id VARCHAR(64) NOT NULL,
    payment_order_no VARCHAR(64) NOT NULL,
    order_id VARCHAR(64) NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    platform VARCHAR(32) NOT NULL,
    trade_type VARCHAR(32) NOT NULL,
    merchant_code VARCHAR(64),
    channel_app_id VARCHAR(64),
    channel_merchant_id VARCHAR(64),
    subject VARCHAR(128) NOT NULL,
    description VARCHAR(255),
    currency VARCHAR(16) NOT NULL DEFAULT 'CNY',
    request_amount_fen BIGINT NOT NULL,
    success_amount_fen BIGINT NOT NULL DEFAULT 0,
    payer_id VARCHAR(128),
    client_ip VARCHAR(64),
    notify_url VARCHAR(255),
    channel_order_no VARCHAR(64),
    channel_prepay_id VARCHAR(128),
    code_url TEXT,
    status VARCHAR(32) NOT NULL,
    fail_code VARCHAR(64),
    fail_message VARCHAR(255),
    expired_time TIMESTAMPTZ,
    success_time TIMESTAMPTZ,
    closed_time TIMESTAMPTZ,
    last_notify_time TIMESTAMPTZ,
    ext_json JSONB,
    version INTEGER NOT NULL DEFAULT 0,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_biz_payment_order_id UNIQUE (payment_order_id),
    CONSTRAINT uk_biz_payment_order_no UNIQUE (payment_order_no),
    CONSTRAINT uk_biz_payment_order_channel_order_no UNIQUE (channel_order_no),
    CONSTRAINT fk_biz_payment_order_order_id FOREIGN KEY (order_id) REFERENCES biz_order(order_id),
    CONSTRAINT ck_biz_payment_order_amount_non_negative CHECK (
        request_amount_fen >= 0
        AND success_amount_fen >= 0
    )
);

CREATE INDEX IF NOT EXISTS idx_biz_payment_order_order_no ON biz_payment_order (order_no);
CREATE INDEX IF NOT EXISTS idx_biz_payment_order_platform ON biz_payment_order (platform);
CREATE INDEX IF NOT EXISTS idx_biz_payment_order_status ON biz_payment_order (status);
CREATE INDEX IF NOT EXISTS idx_biz_payment_order_success_time ON biz_payment_order (success_time DESC);
CREATE INDEX IF NOT EXISTS idx_biz_payment_order_created_at ON biz_payment_order (created_at DESC);

COMMENT ON TABLE biz_payment_order IS '业务支付单表';
COMMENT ON COLUMN biz_payment_order.id IS '数据库自增主键';
COMMENT ON COLUMN biz_payment_order.payment_order_id IS '系统内部稳定支付单ID';
COMMENT ON COLUMN biz_payment_order.payment_order_no IS '系统支付单号';
COMMENT ON COLUMN biz_payment_order.order_id IS '关联业务订单ID';
COMMENT ON COLUMN biz_payment_order.order_no IS '关联业务订单号';
COMMENT ON COLUMN biz_payment_order.platform IS '支付平台，如 WECHAT_PAY';
COMMENT ON COLUMN biz_payment_order.trade_type IS '支付场景，如 NATIVE/JSAPI';
COMMENT ON COLUMN biz_payment_order.merchant_code IS '内部商户配置编码';
COMMENT ON COLUMN biz_payment_order.channel_app_id IS '渠道应用ID，如微信 appid';
COMMENT ON COLUMN biz_payment_order.channel_merchant_id IS '渠道商户号';
COMMENT ON COLUMN biz_payment_order.subject IS '支付标题';
COMMENT ON COLUMN biz_payment_order.description IS '支付描述';
COMMENT ON COLUMN biz_payment_order.currency IS '币种，首期默认 CNY';
COMMENT ON COLUMN biz_payment_order.request_amount_fen IS '请求支付金额，单位分';
COMMENT ON COLUMN biz_payment_order.success_amount_fen IS '支付成功金额，单位分';
COMMENT ON COLUMN biz_payment_order.payer_id IS '支付人标识，如 openid';
COMMENT ON COLUMN biz_payment_order.client_ip IS '客户端IP';
COMMENT ON COLUMN biz_payment_order.notify_url IS '支付回调地址';
COMMENT ON COLUMN biz_payment_order.channel_order_no IS '渠道侧订单号/交易单号';
COMMENT ON COLUMN biz_payment_order.channel_prepay_id IS '渠道预下单标识';
COMMENT ON COLUMN biz_payment_order.code_url IS '扫码支付地址';
COMMENT ON COLUMN biz_payment_order.status IS '支付单状态，如 INIT/WAIT_PAY/SUCCESS';
COMMENT ON COLUMN biz_payment_order.fail_code IS '失败码';
COMMENT ON COLUMN biz_payment_order.fail_message IS '失败描述';
COMMENT ON COLUMN biz_payment_order.expired_time IS '支付单过期时间';
COMMENT ON COLUMN biz_payment_order.success_time IS '支付成功时间';
COMMENT ON COLUMN biz_payment_order.closed_time IS '支付单关闭时间';
COMMENT ON COLUMN biz_payment_order.last_notify_time IS '最后一次收到支付通知的时间';
COMMENT ON COLUMN biz_payment_order.ext_json IS '扩展字段JSON';
COMMENT ON COLUMN biz_payment_order.version IS '乐观锁版本号';
COMMENT ON COLUMN biz_payment_order.created_by IS '创建人';
COMMENT ON COLUMN biz_payment_order.updated_by IS '更新人';
COMMENT ON COLUMN biz_payment_order.created_at IS '创建时间';
COMMENT ON COLUMN biz_payment_order.updated_at IS '更新时间';
