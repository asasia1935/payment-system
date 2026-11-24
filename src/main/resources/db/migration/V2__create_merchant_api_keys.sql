-- V1 remains unchanged; test keys are provisioned internally until account management exists.
CREATE TABLE merchant_api_keys (
    key_id UUID PRIMARY KEY,
    merchant_id UUID NOT NULL REFERENCES merchants(merchant_id),
    secret_hash BYTEA NOT NULL,
    environment VARCHAR(10) NOT NULL CHECK (environment IN ('TEST')),
    status VARCHAR(10) NOT NULL CHECK (status IN ('ACTIVE', 'REVOKED')),
    created_by UUID NULL,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NULL,
    revoked_at TIMESTAMPTZ NULL,
    CONSTRAINT ck_api_key_hash_length CHECK (octet_length(secret_hash) = 32)
);

CREATE INDEX ix_merchant_api_keys_merchant_id ON merchant_api_keys(merchant_id);

CREATE TABLE merchant_api_key_scopes (
    key_id UUID NOT NULL REFERENCES merchant_api_keys(key_id) ON DELETE CASCADE,
    scope VARCHAR(30) NOT NULL CHECK (scope IN ('PAYMENT_CREATE', 'PAYMENT_READ', 'PAYMENT_CANCEL')),
    PRIMARY KEY (key_id, scope)
);
