-- Converte a coluna status de tipo ENUM nativo do PostgreSQL para VARCHAR,
-- que é o que o Hibernate espera ao usar @Enumerated(EnumType.STRING)
ALTER TABLE logistics_orders
    ALTER COLUMN status TYPE VARCHAR(20) USING status::text;

ALTER TABLE logistics_orders
    ALTER COLUMN status SET DEFAULT 'RECEIVED';

DROP TYPE IF EXISTS logistics_status;
