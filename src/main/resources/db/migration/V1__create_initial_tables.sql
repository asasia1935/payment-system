CREATE TABLE merchants (
    merchant_id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK ( status IN ('ACTIVE', 'SUSPENDED') ),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE payments (
    payment_id UUID PRIMARY KEY,
    merchant_id UUID NOT NULL,
    order_id VARCHAR(100) NOT NULL,
    amount BIGINT NOT NULL CHECK ( amount > 0 ),
    currency VARCHAR(3) NOT NULL CHECK (currency = 'KRW'),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uq_payments_merchant_order
        UNIQUE (merchant_id, order_id),

    CONSTRAINT fk_payments_merchant
        FOREIGN KEY (merchant_id)
            REFERENCES merchants(merchant_id)
);