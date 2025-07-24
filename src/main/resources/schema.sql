CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS quotes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    brand VARCHAR(50),
    model VARCHAR(50),
    year INT,
    usage_type VARCHAR(20),
    driver_age INT,
    base_premium NUMERIC(10,2),
    adjustment_amount NUMERIC(10,2),
    total_premium NUMERIC(10,2),
    created_at TIMESTAMP
);
