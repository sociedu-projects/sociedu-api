-- Tất cả khóa chính & khóa ngoại tham chiếu dùng UUID (PostgreSQL gen_random_uuid).
-- Chạy trên DB trống hoặc drop schema cũ trước khi migrate dữ liệu.

-- ==========================================
-- IDENTITY & AUTHENTICATION
-- ==========================================

CREATE TYPE user_status AS ENUM ('pending', 'active', 'suspended');

CREATE TABLE roles
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE capabilities
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE role_capabilities
(
    role_id       UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    capability_id UUID NOT NULL REFERENCES capabilities (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, capability_id)
);

CREATE TABLE users
(
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email          VARCHAR(255) UNIQUE,
    email_verified BOOLEAN     DEFAULT FALSE,
    phone_number   VARCHAR(20) UNIQUE,
    phone_verified BOOLEAN     DEFAULT FALSE,
    date_of_birth  DATE,
    status         user_status DEFAULT 'pending',
    created_at     TIMESTAMP   DEFAULT NOW(),
    updated_at     TIMESTAMP   DEFAULT NOW()
);

CREATE TABLE user_roles
(
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE user_credentials
(
    user_id       UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    password_hash TEXT NOT NULL,
    created_at    TIMESTAMP DEFAULT NOW(),
    updated_at    TIMESTAMP DEFAULT NOW()
);

CREATE TABLE refresh_tokens
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID              NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token      VARCHAR(512) UNIQUE NOT NULL,
    expires_at TIMESTAMP           NOT NULL,
    revoked    BOOLEAN   DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE otp_tokens
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    code       VARCHAR(6)  NOT NULL,
    type       VARCHAR(50) NOT NULL,
    expires_at TIMESTAMP   NOT NULL,
    used       BOOLEAN   DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- ==========================================
-- CENTRALIZED FILE MANAGEMENT
-- ==========================================

CREATE TABLE files
(
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    uploader_id      UUID,

    file_name        VARCHAR(255) NOT NULL,
    file_url         TEXT         NOT NULL,
    mime_type        VARCHAR(100) NOT NULL,
    file_size        BIGINT       NOT NULL,

    storage_provider VARCHAR(50)  NOT NULL,
    visibility       VARCHAR(20) DEFAULT 'private',

    entity_type      VARCHAR(50),
    entity_id        UUID,

    deleted_at       TIMESTAMP,
    created_at       TIMESTAMP   DEFAULT NOW()
);

-- ==========================================
-- USER PROFILE & PROFESSIONAL INFORMATION
-- ==========================================

CREATE TABLE user_profiles
(
    user_id        UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    first_name     VARCHAR(50),
    last_name      VARCHAR(50),
    headline       VARCHAR(150),
    avatar_file_id UUID,
    bio            TEXT,
    location       VARCHAR(255),
    created_at     TIMESTAMP DEFAULT NOW(),
    updated_at     TIMESTAMP DEFAULT NOW()
);

CREATE TABLE user_languages
(
    id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id  UUID       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    language VARCHAR(100) NOT NULL,
    level    VARCHAR(30) CHECK (level IN ('beginner', 'intermediate', 'advanced', 'fluent', 'native'))
);

CREATE TABLE user_experiences
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    company     VARCHAR(255),
    position    VARCHAR(255),
    start_date  DATE   NOT NULL,
    end_date    DATE,
    is_current  BOOLEAN DEFAULT FALSE,
    description TEXT
);

CREATE TABLE universities
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE fields_of_study
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) UNIQUE NOT NULL,
    slug VARCHAR(255) UNIQUE
);

CREATE TABLE user_educations
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    university_id UUID REFERENCES universities (id),
    major_id      UUID REFERENCES fields_of_study (id),
    degree        VARCHAR(100),
    start_date    DATE,
    end_date      DATE,
    is_current    BOOLEAN   DEFAULT FALSE,
    description   TEXT,
    created_at    TIMESTAMP DEFAULT NOW()
);

CREATE TABLE user_certificates
(
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id            UUID       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name               VARCHAR(255) NOT NULL,
    organization       VARCHAR(255),
    issue_date         DATE         NOT NULL,
    expiration_date    DATE,
    credential_file_id UUID,
    description        TEXT,
    created_at         TIMESTAMP DEFAULT NOW()
);

-- ==========================================
-- MENTOR SERVICE PACKAGES
-- ==========================================

CREATE TABLE service_packages
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mentor_id   UUID       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    is_active   BOOLEAN   DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE service_package_versions
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    package_id    UUID         NOT NULL REFERENCES service_packages (id) ON DELETE CASCADE,
    price         DECIMAL(19, 2) NOT NULL,
    duration      INT            NOT NULL,
    delivery_type VARCHAR(50),
    is_default    BOOLEAN   DEFAULT TRUE,
    created_at    TIMESTAMP DEFAULT NOW()
);

CREATE TABLE package_curriculums
(
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    package_version_id UUID       NOT NULL REFERENCES service_package_versions (id) ON DELETE CASCADE,
    title              VARCHAR(255) NOT NULL,
    description        TEXT,
    order_index        INT          NOT NULL,
    duration           INT,
    created_at         TIMESTAMP DEFAULT NOW()
);

-- ==========================================
-- ORDER & PAYMENT
-- ==========================================

CREATE TYPE order_status AS ENUM ('pending_payment', 'paid', 'failed', 'canceled', 'refunded');

CREATE TABLE orders
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_id     UUID         NOT NULL REFERENCES users (id),
    service_id   UUID         NOT NULL REFERENCES service_package_versions (id),
    status       order_status DEFAULT 'pending_payment',
    total_amount DECIMAL(10, 2) NOT NULL,
    paid_at      TIMESTAMP,
    created_at   TIMESTAMP    DEFAULT NOW()
);

CREATE TYPE payment_status AS ENUM ('pending', 'success', 'failed');

CREATE TABLE payment_transactions
(
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id                UUID         NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    provider                VARCHAR(50)    NOT NULL,
    provider_transaction_id VARCHAR(255),
    amount                  DECIMAL(19, 2) NOT NULL,
    status                  payment_status DEFAULT 'pending',
    raw_response            JSONB,
    created_at              TIMESTAMP      DEFAULT NOW()
);

-- ==========================================
-- BOOKING & SESSION
-- ==========================================

CREATE TYPE booking_status AS ENUM ('pending', 'scheduled', 'in_progress', 'completed', 'canceled', 'refunded');

CREATE TABLE bookings
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id   UUID UNIQUE REFERENCES orders (id),
    buyer_id   UUID NOT NULL REFERENCES users (id),
    mentor_id  UUID NOT NULL REFERENCES users (id),
    package_id UUID NOT NULL REFERENCES service_packages (id),
    status     booking_status DEFAULT 'pending',
    created_at TIMESTAMP      DEFAULT NOW()
);

CREATE TYPE session_status AS ENUM ('pending', 'scheduled', 'completed', 'no_show', 'canceled');

CREATE TABLE booking_sessions
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id    UUID    NOT NULL REFERENCES bookings (id) ON DELETE CASCADE,
    curriculum_id UUID    NOT NULL REFERENCES package_curriculums (id),
    title         VARCHAR(255),
    scheduled_at  TIMESTAMP NOT NULL,
    completed_at  TIMESTAMP,
    status        session_status DEFAULT 'pending',
    meeting_url   TEXT,
    created_at    TIMESTAMP      DEFAULT NOW()
);

CREATE TABLE booking_session_evidences
(
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_session_id UUID NOT NULL REFERENCES booking_sessions (id) ON DELETE CASCADE,
    uploaded_by        UUID NOT NULL REFERENCES users (id),
    file_id            UUID,
    description        VARCHAR(255),
    created_at         TIMESTAMP DEFAULT NOW()
);

-- ==========================================
-- MESSAGING SYSTEM
-- ==========================================

CREATE TYPE conversation_type AS ENUM ('general', 'booking', 'support');

CREATE TABLE conversations
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type       conversation_type DEFAULT 'general',
    booking_id UUID,
    created_at TIMESTAMP         DEFAULT NOW()
);

CREATE TABLE conversation_participants
(
    conversation_id UUID NOT NULL REFERENCES conversations (id) ON DELETE CASCADE,
    user_id         UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    joined_at       TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (conversation_id, user_id)
);

CREATE TYPE message_type AS ENUM ('text', 'image', 'file', 'system');

CREATE TABLE messages
(
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES conversations (id) ON DELETE CASCADE,
    sender_id       UUID NOT NULL REFERENCES users (id),
    content         TEXT,
    type            message_type DEFAULT 'text',
    is_edited       BOOLEAN      DEFAULT FALSE,
    created_at      TIMESTAMP    DEFAULT NOW(),
    updated_at      TIMESTAMP
);

CREATE TABLE message_attachments
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL REFERENCES messages (id) ON DELETE CASCADE,
    file_id    UUID,
    created_at TIMESTAMP DEFAULT NOW()
);

-- ==========================================
-- REPORT & DISPUTE
-- ==========================================

CREATE TYPE report_status AS ENUM ('open', 'under_review', 'resolved', 'rejected');
CREATE TYPE report_type AS ENUM ('user', 'message', 'booking', 'session', 'review', 'comment');

CREATE TABLE reports
(
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    reporter_id      UUID       NOT NULL REFERENCES users (id),
    reported_user_id UUID REFERENCES users (id),

    type             report_type  NOT NULL,
    entity_id        UUID       NOT NULL,

    reason           VARCHAR(255) NOT NULL,
    description      TEXT,

    status           report_status DEFAULT 'open',
    created_at       TIMESTAMP     DEFAULT NOW(),
    resolved_at      TIMESTAMP,
    resolved_by      UUID REFERENCES users (id),
    resolution_note  TEXT
);

CREATE TABLE report_evidences
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id   UUID NOT NULL REFERENCES reports (id) ON DELETE CASCADE,
    file_id     UUID NOT NULL,
    description VARCHAR(255),
    uploaded_by UUID NOT NULL REFERENCES users (id),
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE TYPE dispute_status AS ENUM ('open', 'under_review', 'resolved_buyer', 'resolved_mentor', 'partial_refund', 'closed');

CREATE TABLE disputes
(
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id       UUID REFERENCES reports (id),
    booking_id      UUID REFERENCES bookings (id),
    session_id      UUID REFERENCES booking_sessions (id),
    raised_by       UUID       NOT NULL REFERENCES users (id),
    reason          VARCHAR(255) NOT NULL,
    description     TEXT,
    status          dispute_status DEFAULT 'open',
    resolution_note TEXT,
    created_at      TIMESTAMP      DEFAULT NOW(),
    resolved_at     TIMESTAMP,
    resolved_by     UUID REFERENCES users (id)
);

-- ==========================================
-- APP: mentor directory & tiến độ mentee
-- ==========================================

CREATE TABLE mentor_profiles
(
    user_id              UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    headline             VARCHAR(255),
    expertise            TEXT,
    base_price           DECIMAL(19, 2),
    rating_avg           REAL,
    sessions_completed     INT,
    verification_status  VARCHAR(50),
    created_at           TIMESTAMP DEFAULT NOW(),
    updated_at           TIMESTAMP DEFAULT NOW()
);

CREATE TABLE mentee_progress_reports
(
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mentee_id        UUID       NOT NULL REFERENCES users (id),
    mentor_id        UUID       NOT NULL REFERENCES users (id),
    title            VARCHAR(255) NOT NULL,
    content          TEXT         NOT NULL,
    attachment_url   VARCHAR(500),
    status           VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    mentor_feedback  TEXT,
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP DEFAULT NOW()
);
