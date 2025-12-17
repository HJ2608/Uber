-- DROP TABLE IF EXISTS rides CASCADE;
-- DROP TABLE IF EXISTS driver_locations CASCADE;
-- DROP TABLE IF EXISTS otps CASCADE;
-- DROP TABLE IF EXISTS users CASCADE;
-- DROP TYPE IF EXISTS otp_purpose;
-- DROP TABLE IF EXISTS drivers CASCADE;
-- DROP TABLE IF EXISTS users CASCADE;
-- DROP TABLE IF EXISTS cabs CASCADE;

-- DELETE FROM rides WHERE id = 2;

CREATE TABLE IF NOT EXISTS users
(
    id    SERIAL PRIMARY KEY,
    first_name VARCHAR(250) NOT NULL,
    last_name  VARCHAR(250) NOT NULL,
    mobile_num VARCHAR(20)  NOT NULL UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS cabs
(
    id          SERIAL PRIMARY KEY,
    registration_no VARCHAR(20)  NOT NULL UNIQUE,
    model           VARCHAR(100) NOT NULL,
    color           VARCHAR(50),
    cab_type         VARCHAR(20)  DEFAULT 'MINI',
    is_active       BOOLEAN      DEFAULT TRUE,
    CONSTRAINT cab_type_check CHECK (cab_type IN ('MINI','SEDAN','SUV'))
);

CREATE TABLE IF NOT EXISTS drivers
(
    id      SERIAL PRIMARY KEY ,
    name           VARCHAR(250) NOT NULL,
    license_no     VARCHAR(50)  NOT NULL,
    avg_rating     NUMERIC(3, 2) DEFAULT 0.0,
    rating_count   INT           DEFAULT 0,
    cab_id         INT          NOT NULL REFERENCES cabs (id)
);

-- CREATE TYPE otp_purpose AS ENUM ('LOGIN', 'SIGNUP', 'FORGOT_PASSWORD', 'RIDE_START');

CREATE TABLE IF NOT EXISTS otps
(
    id     SERIAL PRIMARY KEY,
    user_id    INT         NOT NULL REFERENCES users (id),
    otp_code   VARCHAR(6)  NOT NULL, -- e.g. "123456"
    purpose    VARCHAR(10) NOT NULL DEFAULT('LOGIN'),
    created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP   NOT NULL,
    used_at    TIMESTAMP,            -- NULL = not used yet
    is_valid   BOOLEAN     NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS driver_locations
(
    driver_id  INT PRIMARY KEY REFERENCES drivers (id),
    lat        DECIMAL(9, 6) NOT NULL,
    lng        DECIMAL(9, 6) NOT NULL,
    updated_at TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);



CREATE TABLE IF NOT EXISTS rides
(
    id        SERIAL PRIMARY KEY,
    cust_id        INT ,          --NOT NULL,
    driver_id      INT ,
    cab_id         INT REFERENCES cabs (id),
    pickup_lat     DECIMAL(9, 6) NOT NULL,
    pickup_lng     DECIMAL(9, 6) NOT NULL,
    drop_lat       DECIMAL(9, 6) NOT NULL,
    drop_lng       DECIMAL(9, 6) NOT NULL,
    otp_id         INT,
    otp_verified   BOOLEAN     DEFAULT FALSE,
    estimated_fare NUMERIC(10,2),
    final_fare     NUMERIC(10, 2),
    started_on     TIMESTAMP,
    ended_on       TIMESTAMP,
    payment_status VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50)   NOT NULL DEFAULT 'Cash',
    status         VARCHAR(20) DEFAULT 'REQUESTED',
    CONSTRAINT fk_cust_id
        FOREIGN KEY (cust_id) REFERENCES users (id),
    CONSTRAINT fk_driver_id
        FOREIGN KEY (driver_id) REFERENCES drivers (id),
    CONSTRAINT fk_otp_id
        FOREIGN KEY (otp_id) REFERENCES otps (id)
);
