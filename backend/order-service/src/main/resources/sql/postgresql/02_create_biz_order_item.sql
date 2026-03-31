-- PostgreSQL DDL for order item snapshot table.
-- Item rows keep product/SKU snapshots so historical orders stay stable even if
-- upstream product definitions are changed later.

CREATE TABLE IF NOT EXISTS biz_order_item (
    id BIGSERIAL PRIMARY KEY,
    order_item_id VARCHAR(64) NOT NULL,
    order_id VARCHAR(64) NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    line_no INTEGER NOT NULL,
    item_type VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
    product_id VARCHAR(64),
    product_code VARCHAR(64),
    product_name VARCHAR(255) NOT NULL,
    sku_id VARCHAR(64),
    sku_code VARCHAR(64),
    sku_name VARCHAR(255),
    quantity BIGINT NOT NULL,
    currency VARCHAR(16) NOT NULL DEFAULT 'CNY',
    unit_price_fen BIGINT NOT NULL,
    original_amount_fen BIGINT NOT NULL,
    discount_amount_fen BIGINT NOT NULL DEFAULT 0,
    payable_amount_fen BIGINT NOT NULL,
    paid_amount_fen BIGINT NOT NULL DEFAULT 0,
    refund_amount_fen BIGINT NOT NULL DEFAULT 0,
    item_status VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
    remark VARCHAR(255),
    ext_json JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_biz_order_item_order_item_id UNIQUE (order_item_id),
    CONSTRAINT uk_biz_order_item_order_line UNIQUE (order_id, line_no),
    CONSTRAINT fk_biz_order_item_order_id FOREIGN KEY (order_id) REFERENCES biz_order(order_id),
    CONSTRAINT ck_biz_order_item_amount_non_negative CHECK (
        quantity > 0
        AND unit_price_fen >= 0
        AND original_amount_fen >= 0
        AND discount_amount_fen >= 0
        AND payable_amount_fen >= 0
        AND paid_amount_fen >= 0
        AND refund_amount_fen >= 0
    )
);

CREATE INDEX IF NOT EXISTS idx_biz_order_item_order_no ON biz_order_item (order_no);
CREATE INDEX IF NOT EXISTS idx_biz_order_item_product_id ON biz_order_item (product_id);
CREATE INDEX IF NOT EXISTS idx_biz_order_item_sku_id ON biz_order_item (sku_id);
CREATE INDEX IF NOT EXISTS idx_biz_order_item_created_at ON biz_order_item (created_at DESC);

COMMENT ON TABLE biz_order_item IS '业务订单明细表';
COMMENT ON COLUMN biz_order_item.id IS '数据库自增主键';
COMMENT ON COLUMN biz_order_item.order_item_id IS '系统内部稳定订单明细ID';
COMMENT ON COLUMN biz_order_item.order_id IS '关联业务订单ID';
COMMENT ON COLUMN biz_order_item.order_no IS '关联业务订单号';
COMMENT ON COLUMN biz_order_item.line_no IS '订单行号，从1开始';
COMMENT ON COLUMN biz_order_item.item_type IS '明细类型，如 NORMAL/GIFT/SERVICE';
COMMENT ON COLUMN biz_order_item.product_id IS '商品ID';
COMMENT ON COLUMN biz_order_item.product_code IS '商品编码';
COMMENT ON COLUMN biz_order_item.product_name IS '商品名称快照';
COMMENT ON COLUMN biz_order_item.sku_id IS 'SKU ID';
COMMENT ON COLUMN biz_order_item.sku_code IS 'SKU 编码';
COMMENT ON COLUMN biz_order_item.sku_name IS 'SKU 名称快照';
COMMENT ON COLUMN biz_order_item.quantity IS '购买数量';
COMMENT ON COLUMN biz_order_item.currency IS '币种，首期默认 CNY';
COMMENT ON COLUMN biz_order_item.unit_price_fen IS '单价，单位分';
COMMENT ON COLUMN biz_order_item.original_amount_fen IS '原始金额，单位分';
COMMENT ON COLUMN biz_order_item.discount_amount_fen IS '优惠金额，单位分';
COMMENT ON COLUMN biz_order_item.payable_amount_fen IS '应付金额，单位分';
COMMENT ON COLUMN biz_order_item.paid_amount_fen IS '已支付金额，单位分';
COMMENT ON COLUMN biz_order_item.refund_amount_fen IS '已退款金额，单位分';
COMMENT ON COLUMN biz_order_item.item_status IS '明细状态，首期默认 NORMAL';
COMMENT ON COLUMN biz_order_item.remark IS '备注';
COMMENT ON COLUMN biz_order_item.ext_json IS '扩展字段JSON';
COMMENT ON COLUMN biz_order_item.created_at IS '创建时间';
COMMENT ON COLUMN biz_order_item.updated_at IS '更新时间';
