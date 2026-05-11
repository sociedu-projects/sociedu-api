-- ==========================================
-- INIT DATA
-- ==========================================

INSERT INTO roles (name)
VALUES ('USER'),
       ('MENTOR'),
       ('ADMIN');

INSERT INTO capabilities (name)
VALUES

-- USER
('UPDATE_PROFILE'),
('VIEW_PROFILE'),
('BOOK_SESSION'),
('CANCEL_BOOKING'),
('WRITE_REVIEW'),

-- MENTOR
('CREATE_SERVICE_PACKAGE'),
('UPDATE_OWN_SERVICE_PACKAGE'),
('DELETE_OWN_SERVICE_PACKAGE'),
('MANAGE_PACKAGE_CURRICULUM'),
('VIEW_OWN_BOOKINGS'),
('MANAGE_OWN_BOOKINGS'),
('MANAGE_SESSIONS'),
('VIEW_EARNINGS'),

-- BOOKING
('VIEW_BOOKING'),
('JOIN_SESSION'),
('START_SESSION'),
('COMPLETE_SESSION'),

-- PAYMENT
('CREATE_PAYMENT'),
('VIEW_PAYMENT'),
('REQUEST_PAYOUT'),
('VIEW_PAYOUT'),
('REFUND_REQUEST'),

-- CHAT
('SEND_MESSAGE'),
('VIEW_CONVERSATION'),
('UPLOAD_ATTACHMENT'),

-- REPORT / DISPUTE
('CREATE_REPORT'),
('VIEW_OWN_REPORT'),
('CREATE_DISPUTE'),
('VIEW_OWN_DISPUTE'),

-- ADMIN
('MANAGE_USERS'),
('MANAGE_MENTORS'),
('MANAGE_ALL_BOOKINGS'),
('MANAGE_PAYMENTS'),
('RESOLVE_REPORT'),
('RESOLVE_DISPUTE'),
('VIEW_SYSTEM_METRICS'),
('MANAGE_ALL');

INSERT INTO role_capabilities (role_id, capability_id)
SELECT (SELECT id FROM roles WHERE name = 'USER'),
       id
FROM capabilities
WHERE name IN (
               'UPDATE_PROFILE',
               'VIEW_PROFILE',
               'BOOK_SESSION',
               'CANCEL_BOOKING',
               'WRITE_REVIEW',
               'VIEW_BOOKING',
               'JOIN_SESSION',
               'CREATE_PAYMENT',
               'VIEW_PAYMENT',
               'SEND_MESSAGE',
               'VIEW_CONVERSATION',
               'UPLOAD_ATTACHMENT',
               'CREATE_REPORT',
               'VIEW_OWN_REPORT',
               'CREATE_DISPUTE',
               'VIEW_OWN_DISPUTE'
    );

INSERT INTO role_capabilities (role_id, capability_id)
SELECT (SELECT id FROM roles WHERE name = 'MENTOR'),
       id
FROM capabilities
WHERE name IN (
    -- USER basic
               'UPDATE_PROFILE',
               'VIEW_PROFILE',
               'BOOK_SESSION',
               'CANCEL_BOOKING',
               'WRITE_REVIEW',

    -- mentor core
               'CREATE_SERVICE_PACKAGE',
               'UPDATE_OWN_SERVICE_PACKAGE',
               'DELETE_OWN_SERVICE_PACKAGE',
               'MANAGE_PACKAGE_CURRICULUM',
               'VIEW_OWN_BOOKINGS',
               'MANAGE_OWN_BOOKINGS',
               'MANAGE_SESSIONS',
               'VIEW_EARNINGS',

    -- booking/session
               'VIEW_BOOKING',
               'JOIN_SESSION',
               'START_SESSION',
               'COMPLETE_SESSION',

    -- payment
               'VIEW_PAYMENT',
               'REQUEST_PAYOUT',
               'VIEW_PAYOUT',

    -- chat
               'SEND_MESSAGE',
               'VIEW_CONVERSATION',
               'UPLOAD_ATTACHMENT'
    );

INSERT INTO role_capabilities (role_id, capability_id)
SELECT (SELECT id FROM roles WHERE name = 'ADMIN'),
       id
FROM capabilities;