-- Identity & User
CREATE TABLE users
(
    id             BIGSERIAL PRIMARY KEY,

    email          VARCHAR(255) UNIQUE,
    email_verified BOOLEAN     DEFAULT FALSE,

    phone_number   VARCHAR(20) UNIQUE,
    phone_verified BOOLEAN     DEFAULT FALSE,

    status         VARCHAR(50) DEFAULT 'active',

    created_at     TIMESTAMP   DEFAULT NOW(),
    updated_at     TIMESTAMP   DEFAULT NOW()
);

CREATE TABLE user_credentials
(
    user_id       BIGINT PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,

    password_hash TEXT NOT NULL,

    created_at    TIMESTAMP DEFAULT NOW(),
    updated_at    TIMESTAMP DEFAULT NOW()
);

CREATE TABLE auth_providers
(
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT REFERENCES users (id) ON DELETE CASCADE,

    provider         VARCHAR(50)  NOT NULL, -- google, facebook, apple
    provider_user_id VARCHAR(255) NOT NULL,

    email            VARCHAR(255),
    avatar_url       TEXT,

    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP DEFAULT NOW(),

    UNIQUE (provider, provider_user_id)
);

CREATE TABLE roles
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE -- BUYER, CONTRIBUTOR, MENTOR, ADMIN
);

CREATE TABLE user_roles
(
    user_id BIGINT REFERENCES users (id),
    role_id INT REFERENCES roles (id),
    PRIMARY KEY (user_id, role_id)
);

-- Profile
CREATE TABLE user_profiles
(
    user_id    BIGINT PRIMARY KEY REFERENCES users (id),
    full_name  VARCHAR(255),
    avatar_url TEXT,
    university VARCHAR(255),
    major      VARCHAR(255),
    year       INT,
    bio        TEXT,
    language   VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE contributor_profiles
(
    user_id     BIGINT PRIMARY KEY REFERENCES users (id),
    store_name  VARCHAR(255),
    expertise   TEXT,
    payout_info JSONB,
    created_at  TIMESTAMP
);

CREATE TABLE mentor_profiles
(
    user_id             BIGINT PRIMARY KEY REFERENCES users (id),
    headline            VARCHAR(255),
    bio                 TEXT,
    expertise_tags      TEXT[],
    experience          TEXT,
    education           TEXT,
    certificates        TEXT,
    verification_status VARCHAR(50), -- pending, approved, rejected
    rating_avg          FLOAT DEFAULT 0,
    total_sessions      INT   DEFAULT 0,
    created_at          TIMESTAMP
);

CREATE TABLE mentor_verifications
(
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT REFERENCES users (id),
    status       VARCHAR(50),
    submitted_at TIMESTAMP,
    reviewed_at  TIMESTAMP,
    reviewer_id  BIGINT
);

-- Document Marketplace
CREATE TABLE document_categories
(
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(255),
    parent_id INT
);

CREATE TABLE documents
(
    id          BIGSERIAL PRIMARY KEY,
    seller_id   BIGINT REFERENCES users (id),
    title       TEXT,
    description TEXT,
    subject     VARCHAR(255),
    university  VARCHAR(255),
    major       VARCHAR(255),
    doc_type    VARCHAR(100),
    price       DECIMAL(10, 2),
    status      VARCHAR(50), -- draft, pending_review, published
    rating_avg  FLOAT DEFAULT 0,
    sales_count INT   DEFAULT 0,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);

CREATE TABLE document_assets
(
    id          BIGSERIAL PRIMARY KEY,
    document_id BIGINT REFERENCES documents (id),
    type        VARCHAR(50), -- preview, main
    file_url    TEXT,
    file_size   BIGINT
);

CREATE TABLE wishlist_items
(
    user_id     BIGINT REFERENCES users (id),
    document_id BIGINT REFERENCES documents (id),
    PRIMARY KEY (user_id, document_id)
);

-- Mentor Marketplace
CREATE TABLE service_packages
(
    id            BIGSERIAL PRIMARY KEY,
    mentor_id     BIGINT REFERENCES users (id),
    name          VARCHAR(255),
    description   TEXT,
    duration      INT,
    price         DECIMAL(10, 2),
    delivery_type VARCHAR(50),
    created_at    TIMESTAMP
);

CREATE TABLE availability_slots
(
    id         BIGSERIAL PRIMARY KEY,
    mentor_id  BIGINT REFERENCES users (id),
    start_time TIMESTAMP,
    end_time   TIMESTAMP,
    is_booked  BOOLEAN DEFAULT FALSE
);

CREATE TABLE bookings
(
    id         BIGSERIAL PRIMARY KEY,
    buyer_id   BIGINT REFERENCES users (id),
    mentor_id  BIGINT REFERENCES users (id),
    package_id BIGINT REFERENCES service_packages (id),
    slot_id    BIGINT REFERENCES availability_slots (id),
    status     VARCHAR(50), -- scheduled, completed
    created_at TIMESTAMP
);
-- Order + Payment + Escrow
CREATE TABLE orders
(
    id           BIGSERIAL PRIMARY KEY,
    buyer_id     BIGINT REFERENCES users (id),
    type         VARCHAR(50), -- document / mentor
    status       VARCHAR(50),
    total_amount DECIMAL(10, 2),
    created_at   TIMESTAMP
);

CREATE TABLE order_items
(
    id        BIGSERIAL PRIMARY KEY,
    order_id  BIGINT REFERENCES orders (id),
    item_type VARCHAR(50), -- document / booking
    item_id   BIGINT,
    price     DECIMAL(10, 2)
);

CREATE TABLE payment_transactions
(
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT REFERENCES orders (id),
    provider        VARCHAR(50),
    transaction_ref VARCHAR(255),
    status          VARCHAR(50),
    created_at      TIMESTAMP
);

CREATE TABLE escrow
(
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT REFERENCES orders (id),
    amount     DECIMAL(10, 2),
    status     VARCHAR(50), -- holding, released
    created_at TIMESTAMP
);

CREATE TABLE payouts
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT REFERENCES users (id),
    amount     DECIMAL(10, 2),
    status     VARCHAR(50),
    created_at TIMESTAMP
);

-- Trust System
CREATE TABLE reviews
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT,
    target_type VARCHAR(50), -- document / mentor
    target_id   BIGINT,
    rating      INT,
    content     TEXT,
    created_at  TIMESTAMP
);

CREATE TABLE reports
(
    id          BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT,
    target_type VARCHAR(50),
    target_id   BIGINT,
    reason      TEXT,
    status      VARCHAR(50),
    created_at  TIMESTAMP
);

CREATE TABLE disputes
(
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT,
    buyer_id   BIGINT,
    reason     TEXT,
    status     VARCHAR(50),
    resolution TEXT,
    created_at TIMESTAMP
);

-- System
CREATE TABLE notifications
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT,
    type       VARCHAR(100),
    content    TEXT,
    is_read    BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP
);

CREATE TABLE audit_logs
(
    id          BIGSERIAL PRIMARY KEY,
    actor_id    BIGINT,
    action      VARCHAR(255),
    entity_type VARCHAR(100),
    entity_id   BIGINT,
    metadata    JSONB,
    created_at  TIMESTAMP
);
