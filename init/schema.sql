-- ==========================================
-- EXTENSIONS
-- ==========================================
CREATE
EXTENSION IF NOT EXISTS "pgcrypto";

-- ==========================================
-- AUTH
-- ==========================================
CREATE TABLE roles
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) UNIQUE
);

CREATE TABLE capabilities
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) UNIQUE
);

CREATE TABLE role_capabilities
(
    role_id       UUID REFERENCES roles (id) ON DELETE CASCADE,
    capability_id UUID REFERENCES capabilities (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, capability_id)
);

CREATE TABLE users
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    email        VARCHAR(255),
    phone_number VARCHAR(20),

    status       SMALLINT         DEFAULT 0, -- app define

    created_at   TIMESTAMP        DEFAULT NOW(),
    updated_at   TIMESTAMP        DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users (email);

CREATE TABLE user_roles
(
    user_id UUID REFERENCES users (id) ON DELETE CASCADE,
    role_id UUID REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE user_credentials
(
    user_id       UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    password_hash TEXT NOT NULL,
    created_at    TIMESTAMP DEFAULT NOW()
);

-- ==========================================
-- TOKENS
-- ==========================================
CREATE TABLE refresh_tokens
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID REFERENCES users (id) ON DELETE CASCADE,
    token      VARCHAR(512),
    expires_at TIMESTAMP,
    revoked    BOOLEAN          DEFAULT FALSE,
    created_at TIMESTAMP        DEFAULT NOW()
);

CREATE INDEX idx_refresh_user ON refresh_tokens (user_id);

CREATE TABLE otp_tokens
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID REFERENCES users (id) ON DELETE CASCADE,
    code       VARCHAR(6),
    type       SMALLINT,
    expires_at TIMESTAMP,
    used       BOOLEAN          DEFAULT FALSE,
    created_at TIMESTAMP        DEFAULT NOW()
);

-- ==========================================
-- FILES
-- ==========================================
CREATE TABLE files
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    uploader_id UUID REFERENCES users (id),

    file_name   VARCHAR(255),
    file_url    TEXT,
    mime_type   VARCHAR(100),
    file_size   BIGINT,

    entity_type SMALLINT,
    entity_id   UUID,

    created_at  TIMESTAMP        DEFAULT NOW()
);

CREATE INDEX idx_files_uploader ON files (uploader_id);
CREATE INDEX idx_files_entity ON files (entity_type, entity_id);

-- ==========================================
-- PROFILE
-- ==========================================
CREATE TABLE user_profiles
(
    user_id        UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,

    first_name     VARCHAR(50),
    last_name      VARCHAR(50),

    avatar_file_id UUID REFERENCES files (id),

    bio            TEXT,
    created_at     TIMESTAMP DEFAULT NOW()
);

CREATE TABLE universities
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255)
);

CREATE TABLE fields_of_study
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255)
);

CREATE TABLE user_educations
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID REFERENCES users (id) ON DELETE CASCADE,
    university_id UUID REFERENCES universities (id),
    major_id      UUID REFERENCES fields_of_study (id)
);

CREATE TABLE user_experiences
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id     UUID REFERENCES users (id) ON DELETE CASCADE,

    company     VARCHAR(255),
    position    VARCHAR(255),

    start_date  DATE,
    end_date    DATE,
    is_current  BOOLEAN          DEFAULT FALSE,

    description TEXT,

    created_at  TIMESTAMP        DEFAULT NOW()
);

CREATE INDEX idx_user_experience_user ON user_experiences (user_id);

CREATE TABLE user_certificates
(
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id            UUID REFERENCES users (id) ON DELETE CASCADE,

    name               VARCHAR(255),
    organization       VARCHAR(255),

    issue_date         DATE,
    expiration_date    DATE,

    credential_file_id UUID REFERENCES files (id),

    created_at         TIMESTAMP        DEFAULT NOW()
);

CREATE INDEX idx_user_cert_user ON user_certificates (user_id);

CREATE TABLE skills
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) UNIQUE
);

CREATE TABLE user_skills
(
    user_id  UUID REFERENCES users (id) ON DELETE CASCADE,
    skill_id UUID REFERENCES skills (id),
    level    SMALLINT,

    PRIMARY KEY (user_id, skill_id)
);

CREATE TABLE user_projects
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id     UUID REFERENCES users (id) ON DELETE CASCADE,

    name        VARCHAR(255),
    description TEXT,

    project_url TEXT,
    file_id     UUID REFERENCES files (id),

    created_at  TIMESTAMP        DEFAULT NOW()
);
-- ==========================================
-- SERVICE
-- ==========================================
CREATE TABLE service_packages
(
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- identity của "package logic"
    package_group_id UUID NOT NULL,

    mentor_id        UUID REFERENCES users (id),

    name             VARCHAR(255),

    -- versioning
    version          INT  NOT NULL,
    is_active        BOOLEAN          DEFAULT TRUE,

    -- business data
    price            DECIMAL(19, 2),
    duration         INT,

    created_at       TIMESTAMP        DEFAULT NOW()
);

CREATE INDEX idx_package_group ON service_packages (package_group_id);
CREATE INDEX idx_package_mentor ON service_packages (mentor_id);

CREATE TABLE package_curriculums
(
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    package_id UUID         NOT NULL REFERENCES service_packages (id) ON DELETE CASCADE,
    title              VARCHAR(255) NOT NULL,
    description        TEXT,
    order_index        INT          NOT NULL,
    duration           INT,
    created_at         TIMESTAMP        DEFAULT NOW()
);

-- ==========================================
-- ORDER
-- ==========================================
CREATE TABLE orders
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    buyer_id     UUID REFERENCES users (id),
    service_id   UUID REFERENCES service_package_versions (id),

    status       SMALLINT,
    total_amount DECIMAL(10, 2),

    created_at   TIMESTAMP        DEFAULT NOW()
);

CREATE INDEX idx_orders_buyer ON orders (buyer_id);
CREATE INDEX idx_orders_status ON orders (status);

-- ==========================================
-- PAYMENT
-- ==========================================
CREATE TABLE payment_transactions
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    order_id   UUID REFERENCES orders (id) ON DELETE CASCADE,
    amount     DECIMAL(19, 2),

    status     SMALLINT,
    created_at TIMESTAMP        DEFAULT NOW()
);

CREATE INDEX idx_payment_order ON payment_transactions (order_id);

-- ==========================================
-- BOOKING
-- ==========================================
CREATE TABLE bookings
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    order_id   UUID UNIQUE REFERENCES orders (id),
    buyer_id   UUID REFERENCES users (id),
    mentor_id  UUID REFERENCES users (id),
    package_id UUID REFERENCES service_packages (id),

    status     SMALLINT,
    created_at TIMESTAMP        DEFAULT NOW()
);

CREATE INDEX idx_booking_buyer ON bookings (buyer_id);
CREATE INDEX idx_booking_mentor ON bookings (mentor_id);

CREATE TABLE booking_sessions
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    booking_id   UUID REFERENCES bookings (id) ON DELETE CASCADE,
    title        VARCHAR(255),

    status       SMALLINT,
    scheduled_at TIMESTAMP
);

CREATE INDEX idx_session_booking ON booking_sessions (booking_id);

-- ==========================================
-- MESSAGING
-- ==========================================
CREATE TABLE conversations
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    type       SMALLINT,
    booking_id UUID REFERENCES bookings (id),

    created_at TIMESTAMP        DEFAULT NOW()
);

CREATE TABLE conversation_participants
(
    conversation_id UUID REFERENCES conversations (id) ON DELETE CASCADE,
    user_id         UUID REFERENCES users (id) ON DELETE CASCADE,
    PRIMARY KEY (conversation_id, user_id)
);

CREATE TABLE messages
(
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    conversation_id UUID REFERENCES conversations (id) ON DELETE CASCADE,
    sender_id       UUID REFERENCES users (id),

    content         TEXT,
    created_at      TIMESTAMP        DEFAULT NOW()
);

CREATE INDEX idx_messages_conv ON messages (conversation_id);

CREATE TABLE message_attachments
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID REFERENCES messages (id) ON DELETE CASCADE,
    file_id    UUID REFERENCES files (id)
);

-- ==========================================
-- REPORT
-- ==========================================
CREATE TABLE reports
(
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    reporter_id      UUID REFERENCES users (id),
    reported_user_id UUID REFERENCES users (id),

    type             SMALLINT,
    entity_id        UUID,

    status           SMALLINT,
    created_at       TIMESTAMP        DEFAULT NOW()
);

CREATE TABLE report_evidences
(
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id UUID REFERENCES reports (id) ON DELETE CASCADE,
    file_id   UUID REFERENCES files (id)
);

-- ==========================================
-- STATS (PRE-COMPUTE)
-- ==========================================
CREATE TABLE user_stats
(
    user_id            UUID PRIMARY KEY REFERENCES users (id),

    total_orders       INT            DEFAULT 0,
    total_spent        DECIMAL(19, 2) DEFAULT 0,

    total_sessions     INT            DEFAULT 0,
    completed_sessions INT            DEFAULT 0,

    updated_at         TIMESTAMP      DEFAULT NOW()
);

CREATE TABLE mentor_stats
(
    mentor_id      UUID PRIMARY KEY REFERENCES users (id),

    total_students INT            DEFAULT 0,
    total_sessions INT            DEFAULT 0,

    total_revenue  DECIMAL(19, 2) DEFAULT 0,
    rating_avg     REAL           DEFAULT 0,

    updated_at     TIMESTAMP      DEFAULT NOW()
);

CREATE TABLE service_stats
(
    package_id    UUID PRIMARY KEY REFERENCES service_packages (id),

    total_orders  INT            DEFAULT 0,
    total_revenue DECIMAL(19, 2) DEFAULT 0,

    updated_at    TIMESTAMP      DEFAULT NOW()
);

CREATE TABLE system_stats
(
    date               DATE PRIMARY KEY,

    new_users          INT            DEFAULT 0,
    active_users       INT            DEFAULT 0,

    total_orders       INT            DEFAULT 0,
    successful_orders  INT            DEFAULT 0,
    failed_orders      INT            DEFAULT 0,

    revenue            DECIMAL(19, 2) DEFAULT 0,

    total_sessions     INT            DEFAULT 0,
    completed_sessions INT            DEFAULT 0,

    new_mentors        INT            DEFAULT 0,

    created_at         TIMESTAMP      DEFAULT NOW()
);

-- ==========================================
-- EVENT LOG (ANALYTICS)
-- ==========================================
CREATE TABLE system_events
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    event_type SMALLINT,
    user_id    UUID,
    entity_id  UUID,

    metadata   JSONB,
    created_at TIMESTAMP        DEFAULT NOW()
);

CREATE INDEX idx_events_type ON system_events (event_type);
CREATE INDEX idx_events_user ON system_events (user_id);