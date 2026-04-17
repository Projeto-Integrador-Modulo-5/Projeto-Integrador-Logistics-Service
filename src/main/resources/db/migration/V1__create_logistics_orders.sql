CREATE TYPE logistics_status AS ENUM ('RECEIVED', 'PROCESSING', 'SHIPPED', 'DELIVERED');

CREATE TABLE logistics_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    tracking_code VARCHAR(50),
    status logistics_status NOT NULL DEFAULT 'RECEIVED',
    received_at TIMESTAMP NOT NULL DEFAULT NOW(),
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP
);
