-- PostgreSQL DDL for business order main table.
-- This table is intentionally platform-agnostic so one order can evolve from
-- "preferred pay platform" to "successful pay platform" without hardcoding WeChat.

CREATE TABLE IF NOT EXISTS biz_order (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(64) NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    external_order_no VARCHAR(64),
    biz_type VARCHAR(32) NOT NULL DEFAULT 'PAYMENT',
    order_source VARCHAR(32) NOT NULL DEFAULT 'ADMIN',
    order_title VARCHAR(128) NOT NULL,
    order_description VARCHAR(255),
    buyer_id VARCHAR(64),
    buyer_nickname VARCHAR(128),
    currency VARCHAR(16) NOT NULL DEFAULT 'CNY',
    total_amount_fen BIGINT NOT NULL,
    payable_amount_fen BIGINT NOT NULL,
    paid_amount_fen BIGINT NOT NULL DEFAULT 0,
    refunded_amount_fen BIGINT NOT NULL DEFAULT 0,
    order_status VARCHAR(32) NOT NULL,
    pay_status VARCHAR(32) NOT NULL,
    preferred_pay_platform VARCHAR(32),
    preferred_trade_type VARCHAR(32),
    success_pay_platform VARCHAR(32),
    latest_payment_order_no VARCHAR(64),
    latest_channel_order_no VARCHAR(64),
    paid_time TIMESTAMPTZ,
    expired_time TIMESTAMPTZ,
    closed_time TIMESTAMPTZ,
    cancelled_time TIMESTAMPTZ,
    remark VARCHAR(255),
    ext_json JSONB,
    version INTEGER NOT NULL DEFAULT 0,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_biz_order_order_id UNIQUE (order_id),
    CONSTRAINT uk_biz_order_order_no UNIQUE (order_no),
    CONSTRAINT ck_biz_order_amount_non_negative CHECK (
        total_amount_fen >= 0
        AND payable_amount_fen >= 0
        AND paid_amount_fen >= 0
        AND refunded_amount_fen >= 0
    )
);

CREATE INDEX IF NOT EXISTS idx_biz_order_status ON biz_order (order_status);
CREATE INDEX IF NOT EXISTS idx_biz_order_pay_status ON biz_order (pay_status);
CREATE INDEX IF NOT EXISTS idx_biz_order_buyer_id ON biz_order (buyer_id);
CREATE INDEX IF NOT EXISTS idx_biz_order_created_at ON biz_order (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_biz_order_paid_time ON biz_order (paid_time DESC);
CREATE INDEX IF NOT EXISTS idx_biz_order_latest_payment_order_no ON biz_order (latest_payment_order_no);

COMMENT ON TABLE biz_order IS '业务订单主表';
COMMENT ON COLUMN biz_order.id IS '数据库自增主键';
COMMENT ON COLUMN biz_order.order_id IS '系统内部稳定订单ID，建议使用雪花ID或UUID';
COMMENT ON COLUMN biz_order.order_no IS '对外展示或业务侧使用的订单号';
COMMENT ON COLUMN biz_order.external_order_no IS '外部系统订单号，预留ERP或第三方业务单关联';
COMMENT ON COLUMN biz_order.biz_type IS '业务类型，首期默认 PAYMENT';
COMMENT ON COLUMN biz_order.order_source IS '下单来源，首期默认 ADMIN';
COMMENT ON COLUMN biz_order.order_title IS '订单标题';
COMMENT ON COLUMN biz_order.order_description IS '订单描述';
COMMENT ON COLUMN biz_order.buyer_id IS '购买人ID';
COMMENT ON COLUMN biz_order.buyer_nickname IS '购买人昵称';
COMMENT ON COLUMN biz_order.currency IS '币种，首期默认 CNY';
COMMENT ON COLUMN biz_order.total_amount_fen IS '订单总金额，单位分';
COMMENT ON COLUMN biz_order.payable_amount_fen IS '应付金额，单位分';
COMMENT ON COLUMN biz_order.paid_amount_fen IS '已支付金额，单位分';
COMMENT ON COLUMN biz_order.refunded_amount_fen IS '已退款金额，单位分';
COMMENT ON COLUMN biz_order.order_status IS '订单状态，如 CREATED/WAIT_PAY/PAID/CLOSED';
COMMENT ON COLUMN biz_order.pay_status IS '订单级支付状态，如 UNPAID/PAYING/PAID/REFUNDED';
COMMENT ON COLUMN biz_order.preferred_pay_platform IS '期望支付平台，如 WECHAT_PAY';
COMMENT ON COLUMN biz_order.preferred_trade_type IS '期望支付场景，如 NATIVE/JSAPI';
COMMENT ON COLUMN biz_order.success_pay_platform IS '最终支付成功的平台';
COMMENT ON COLUMN biz_order.latest_payment_order_no IS '最近一次支付单号';
COMMENT ON COLUMN biz_order.latest_channel_order_no IS '最近一次渠道支付单号';
COMMENT ON COLUMN biz_order.paid_time IS '订单支付成功时间';
COMMENT ON COLUMN biz_order.expired_time IS '订单过期时间';
COMMENT ON COLUMN biz_order.closed_time IS '订单关闭时间';
COMMENT ON COLUMN biz_order.cancelled_time IS '订单取消时间';
COMMENT ON COLUMN biz_order.remark IS '业务备注';
COMMENT ON COLUMN biz_order.ext_json IS '扩展字段JSON，预留后续多平台或个性化字段';
COMMENT ON COLUMN biz_order.version IS '乐观锁版本号';
COMMENT ON COLUMN biz_order.created_by IS '创建人';
COMMENT ON COLUMN biz_order.updated_by IS '更新人';
COMMENT ON COLUMN biz_order.created_at IS '创建时间';
COMMENT ON COLUMN biz_order.updated_at IS '更新时间';
