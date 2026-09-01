CREATE TYPE device_state AS ENUM ('AVAILABLE', 'IN_USE', 'INACTIVE');

CREATE TABLE devices (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    brand       VARCHAR(255) NOT NULL,
    state       VARCHAR(50)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_devices_brand ON devices (LOWER(brand));
CREATE INDEX idx_devices_state ON devices (state);
