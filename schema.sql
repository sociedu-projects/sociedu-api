-- ==========================================
-- IDENTITY & AUTHENTICATION
-- ==========================================

CREATE TABLE roles
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL -- BUYER, CONTRIBUTOR, MENTOR, ADMIN
);

CREATE TABLE capabilities
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL -- e.g. UPLOAD_DOCUMENT, BOOK_SESSION, MANAGE_USERS
);

CREATE TABLE role_capabilities
(
    role_id       INT REFERENCES roles (id) ON DELETE CASCADE,
    capability_id INT REFERENCES capabilities (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, capability_id)
);

CREATE TABLE users
(
    id             BIGSERIAL PRIMARY KEY,
    email          VARCHAR(255) UNIQUE,
    email_verified BOOLEAN     DEFAULT FALSE,
    phone_number   VARCHAR(20) UNIQUE,
    phone_verified BOOLEAN     DEFAULT FALSE,
    status         VARCHAR(50) DEFAULT 'pending', -- pending, active, blocked
    created_at     TIMESTAMP   DEFAULT NOW(),
    updated_at     TIMESTAMP   DEFAULT NOW()
);

CREATE TABLE user_roles
(
    user_id BIGINT REFERENCES users (id) ON DELETE CASCADE,
    role_id INT REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE user_credentials
(
    user_id       BIGINT PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    password_hash TEXT NOT NULL,
    created_at    TIMESTAMP DEFAULT NOW(),
    updated_at    TIMESTAMP DEFAULT NOW()
);

CREATE TABLE refresh_tokens
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT REFERENCES users (id) ON DELETE CASCADE,
    token      VARCHAR(512) UNIQUE NOT NULL,
    expires_at TIMESTAMP           NOT NULL,
    revoked    BOOLEAN   DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE otp_tokens
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT REFERENCES users (id) ON DELETE CASCADE,
    code       VARCHAR(6)  NOT NULL,
    type       VARCHAR(50) NOT NULL, -- EMAIL_VERIFY, PASSWORD_RESET
    expires_at TIMESTAMP   NOT NULL,
    used       BOOLEAN   DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- ==========================================
-- USER PROFILE & DETAILS
-- ==========================================

CREATE TABLE user_profiles
(
    user_id    BIGINT PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    full_name  VARCHAR(255),
    avatar_url TEXT,
    bio        TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE user_languages
(
    id      BIGSERIAL PRIMARY KEY,
    user_id BIGINT    REFERENCES users (id) ON DELETE CASCADE,
    language VARCHAR(255),
    level    VARCHAR(255) -- basic, intermediate, fluent, native
);

CREATE TABLE user_experiences
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT    REFERENCES users (id) ON DELETE CASCADE,
    company     VARCHAR(255),
    position    VARCHAR(255),
    start_date  DATE,
    end_date    DATE,
    description TEXT
);

CREATE TABLE user_educations
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT    REFERENCES users (id) ON DELETE CASCADE,
    university  VARCHAR(255),
    major       VARCHAR(255),
    start_year  INT,
    end_year    INT,
    description TEXT
);

CREATE TABLE user_certificates
(
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT    REFERENCES users (id) ON DELETE CASCADE,
    name            VARCHAR(255),
    organization    VARCHAR(255),
    issue_date      DATE,
    expiration_date DATE,
    credential_url  TEXT
);

-- ==========================================
-- DOCUMENT MARKETPLACE
-- ==========================================

CREATE TABLE documents
(
    id           BIGSERIAL PRIMARY KEY,
    seller_id    BIGINT NOT NULL REFERENCES users (id),
    title        TEXT,
    description  TEXT,
    subject      VARCHAR(255),
    university   VARCHAR(255),
    major        VARCHAR(255),
    doc_type     VARCHAR(100), -- @Column(name = "doc_type", length = 100)
    price        DECIMAL(19, 2),
    status       VARCHAR(50) DEFAULT 'draft', -- draft, pending_review, published
    rating_avg   FLOAT       DEFAULT 0,
    sales_count  INT         DEFAULT 0,
    created_at   TIMESTAMP   DEFAULT NOW(),
    updated_at   TIMESTAMP   DEFAULT NOW()
);

CREATE TABLE document_files
(
    id          BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES documents (id) ON DELETE CASCADE,
    file_type   VARCHAR(50), -- preview, full, thumbnail
    file_format VARCHAR(50), -- pdf, docx, zip, image
    file_url    TEXT   NOT NULL,
    file_size   BIGINT,
    sort_order  INT       DEFAULT 0,
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE document_reviews
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users (id),
    document_id BIGINT NOT NULL REFERENCES documents (id) ON DELETE CASCADE,
    rating      INT CHECK (rating BETWEEN 1 AND 5),
    comment     TEXT,
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE document_categories
(
    id        BIGSERIAL PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    parent_id BIGINT REFERENCES document_categories (id)
);

CREATE TABLE wishlist_items
(
    user_id     BIGINT REFERENCES users (id) ON DELETE CASCADE,
    document_id BIGINT REFERENCES documents (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, document_id)
);

-- ==========================================
-- MENTOR MARKETPLACE
-- ==========================================

CREATE TABLE mentor_profiles
(
    user_id             BIGINT PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    headline            VARCHAR(255),
    expertise           TEXT, -- comma separated
    base_price          DECIMAL(19, 2),
    rating_avg          FLOAT       DEFAULT 0,
    sessions_completed  INT         DEFAULT 0,
    verification_status VARCHAR(50) DEFAULT 'pending',
    created_at          TIMESTAMP   DEFAULT NOW(),
    updated_at          TIMESTAMP   DEFAULT NOW()
);

CREATE TABLE service_packages
(
    id            BIGSERIAL PRIMARY KEY,
    mentor_id     BIGINT REFERENCES users (id),
    name          VARCHAR(255),
    description   TEXT,
    duration      INT, -- in minutes
    price         DECIMAL(19, 2),
    delivery_type VARCHAR(255), -- online, chat
    created_at    TIMESTAMP DEFAULT NOW()
);

-- ==========================================
-- ORDER & PAYMENT
-- ==========================================

CREATE TABLE orders
(
    id           BIGSERIAL PRIMARY KEY,
    buyer_id     BIGINT NOT NULL REFERENCES users (id),
    type         VARCHAR(50), -- products / mentor
    status       VARCHAR(50), -- pending, completed, cancelled, refunded
    total_amount DECIMAL(10, 2),
    created_at   TIMESTAMP DEFAULT NOW()
);

CREATE TABLE order_items
(
    id        BIGSERIAL PRIMARY KEY,
    order_id  BIGINT REFERENCES orders (id) ON DELETE CASCADE,
    item_type VARCHAR(50), -- products / booking
    item_id   BIGINT,
    price     DECIMAL(10, 2)
);

CREATE TABLE payouts
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT REFERENCES users (id),
    amount     DECIMAL(10, 2),
    status     VARCHAR(50), -- pending, completed, rejected
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE payment_transactions
(
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT REFERENCES orders (id),
    provider        VARCHAR(50), -- vnpay
    transaction_ref VARCHAR(255),
    status          VARCHAR(50), -- pending, success, failed
    created_at      TIMESTAMP DEFAULT NOW()
);

CREATE TABLE escrow
(
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT REFERENCES orders (id),
    amount     DECIMAL(10, 2),
    status     VARCHAR(50), -- holding, released
    created_at TIMESTAMP DEFAULT NOW()
);

-- ==========================================
-- SEED DATA
-- ==========================================

INSERT INTO roles (name) VALUES ('BUYER'), ('CONTRIBUTOR'), ('MENTOR'), ('ADMIN');

INSERT INTO capabilities (name) VALUES
    ('VIEW_DOCUMENT'), ('PURCHASE_DOCUMENT'), ('BOOK_SESSION'), ('WRITE_REVIEW'),
    ('UPLOAD_DOCUMENT'), ('MANAGE_OWN_DOCUMENTS'),
    ('MANAGE_SESSIONS'), ('VIEW_EARNINGS'),
    ('MANAGE_USERS'), ('MANAGE_ALL');

-- BUYER capabilities (role_id=1)
INSERT INTO role_capabilities (role_id, capability_id)
SELECT 1, id FROM capabilities WHERE name IN ('VIEW_DOCUMENT','PURCHASE_DOCUMENT','BOOK_SESSION','WRITE_REVIEW');

-- CONTRIBUTOR capabilities (role_id=2)
INSERT INTO role_capabilities (role_id, capability_id)
SELECT 2, id FROM capabilities WHERE name IN ('VIEW_DOCUMENT','PURCHASE_DOCUMENT','BOOK_SESSION','WRITE_REVIEW','UPLOAD_DOCUMENT','MANAGE_OWN_DOCUMENTS');

-- MENTOR capabilities (role_id=3)
INSERT INTO role_capabilities (role_id, capability_id)
SELECT 3, id FROM capabilities WHERE name IN ('VIEW_DOCUMENT','PURCHASE_DOCUMENT','BOOK_SESSION','WRITE_REVIEW','MANAGE_SESSIONS','VIEW_EARNINGS');

-- ADMIN capabilities (role_id=4)
INSERT INTO role_capabilities (role_id, capability_id)
SELECT 4, id FROM capabilities;
