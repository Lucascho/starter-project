CREATE TABLE IF NOT EXISTS products (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    price       DOUBLE PRECISION NOT NULL,
    stock       INT NOT NULL,
    created_at  TIMESTAMP DEFAULT now()
);

CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS orders (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id),
    product_id  BIGINT NOT NULL REFERENCES products(id),
    quantity    INT NOT NULL,
    total_price DOUBLE PRECISION NOT NULL,
    created_at  TIMESTAMP DEFAULT now()
);

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_products_name_trgm
    ON products USING gin (lower(name) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_products_price
    ON products (price);

CREATE INDEX IF NOT EXISTS idx_products_created_at
    ON products (created_at);
