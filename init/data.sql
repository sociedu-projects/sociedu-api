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
SELECT 'f93d94ef-1fe8-48c4-b240-d9b1001affb1',
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
SELECT 'd7af84b1-731d-48dc-bcd6-cebe8e78b279',
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
SELECT '29056341-f10b-4d2c-bb32-c6b82cf897bb',
       id
FROM capabilities;