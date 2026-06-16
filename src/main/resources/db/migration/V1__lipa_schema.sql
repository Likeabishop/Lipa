CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Plans
CREATE TABLE IF NOT EXISTS plans (
    plan_id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                 VARCHAR(100)    NOT NULL,
    description          TEXT,
    amount               NUMERIC(12, 2)  NOT NULL,
    currency             VARCHAR(3)      NOT NULL,
    billing_interval     VARCHAR(20)     NOT NULL,
    trial_days           INTEGER,
    active               BOOLEAN         NOT NULL DEFAULT TRUE,
    max_seats            INTEGER,
    stripe_product_id    VARCHAR(100),
    stripe_price_id      VARCHAR(100)    UNIQUE,
    created_at           TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- Customers
CREATE TABLE IF NOT EXISTS customers (
    customer_id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_user_id             VARCHAR(200)    NOT NULL UNIQUE,
    email                        VARCHAR(255)    NOT NULL,
    name                         VARCHAR(255),
    stripe_customer_id           VARCHAR(100)    UNIQUE,
    default_payment_method_id    VARCHAR(100),
    currency                     VARCHAR(3)      NOT NULL,
    tax_exempt                   BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at                   TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                   TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_customers_external_user_id   ON customers (external_user_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_customers_stripe_customer_id ON customers (stripe_customer_id);

-- Subscriptions
CREATE TABLE IF NOT EXISTS subscriptions (
    subscription_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id             UUID            NOT NULL REFERENCES customers (customer_id),
    plan_id                 UUID            NOT NULL REFERENCES plans (plan_id),
    status                  VARCHAR(20)     NOT NULL,
    stripe_subscription_id  VARCHAR(100),
    current_period_start    TIMESTAMPTZ,
    current_period_end      TIMESTAMPTZ,
    trial_end               TIMESTAMPTZ,
    canceled_at             TIMESTAMPTZ,
    cancel_at_period_end    TIMESTAMPTZ         DEFAULT NULL,
    seats                   INTEGER         NOT NULL DEFAULT 1,
    coupon_code             VARCHAR(50),
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_subscriptions_customer_id ON subscriptions (customer_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_status      ON subscriptions (status);
CREATE INDEX IF NOT EXISTS idx_subscriptions_trial_end   ON subscriptions (trial_end);

-- Invoices
CREATE TABLE IF NOT EXISTS invoices (
    invoice_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id         UUID            NOT NULL REFERENCES customers (customer_id),
    subscription_id     UUID            NOT NULL REFERENCES subscriptions (subscription_id),
    status              VARCHAR(20)     NOT NULL,
    stripe_invoice_id   VARCHAR(100),
    invoice_number      VARCHAR(30)     UNIQUE,
    subtotal            NUMERIC(12, 2),
    tax_amount          NUMERIC(12, 2),
    discount_amount     NUMERIC(12, 2),
    total               NUMERIC(12, 2),
    currency            VARCHAR(3)      NOT NULL,
    due_date            TIMESTAMPTZ,
    paid_at             TIMESTAMPTZ,
    hosted_invoice_url  VARCHAR(500),
    line_items          JSONB,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_invoices_customer_id     ON invoices (customer_id);
CREATE INDEX IF NOT EXISTS idx_invoices_subscription_id ON invoices (subscription_id);
CREATE INDEX IF NOT EXISTS idx_invoices_status          ON invoices (status);

-- Payments
CREATE TABLE IF NOT EXISTS payments (
    payment_id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id                  UUID            NOT NULL REFERENCES customers (customer_id),
    invoice_id                   UUID            NOT NULL REFERENCES invoices (invoice_id),
    status                       VARCHAR(25)     NOT NULL,
    amount                       NUMERIC(12, 2)  NOT NULL,
    refunded_amount              NUMERIC(12, 2)  NOT NULL DEFAULT 0.00,
    currency                     VARCHAR(3)      NOT NULL,
    stripe_payment_intent_id     VARCHAR(100),
    stripe_charge_id             VARCHAR(100),
    payment_method_id            VARCHAR(100),
    failure_code                 VARCHAR(100),
    failure_message              VARCHAR(500),
    idempotency_key              VARCHAR(100)    NOT NULL UNIQUE,
    processed_at                 TIMESTAMPTZ,
    created_at                   TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                   TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX        IF NOT EXISTS idx_payments_customer_id           ON payments (customer_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_payments_idempotency_key       ON payments (idempotency_key);
CREATE INDEX        IF NOT EXISTS idx_payments_stripe_payment_intent ON payments (stripe_payment_intent_id);