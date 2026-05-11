-- 1. Gán quyền cho role USER
INSERT INTO
    role_capabilities (role_id, capability_id)
SELECT (
        SELECT id
        FROM roles
        WHERE
            name = 'USER'
    ), id
FROM capabilities
WHERE
    name IN (
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
    )
ON CONFLICT DO NOTHING;

-- 2. Gán quyền cho role MENTOR
INSERT INTO
    role_capabilities (role_id, capability_id)
SELECT (
        SELECT id
        FROM roles
        WHERE
            name = 'MENTOR'
    ), id
FROM capabilities
WHERE
    name IN (
        'UPDATE_PROFILE',
        'VIEW_PROFILE',
        'BOOK_SESSION',
        'CANCEL_BOOKING',
        'WRITE_REVIEW',
        'CREATE_SERVICE_PACKAGE',
        'UPDATE_OWN_SERVICE_PACKAGE',
        'DELETE_OWN_SERVICE_PACKAGE',
        'MANAGE_PACKAGE_CURRICULUM',
        'VIEW_OWN_BOOKINGS',
        'MANAGE_OWN_BOOKINGS',
        'MANAGE_SESSIONS',
        'VIEW_EARNINGS',
        'VIEW_BOOKING',
        'JOIN_SESSION',
        'START_SESSION',
        'COMPLETE_SESSION',
        'VIEW_PAYMENT',
        'REQUEST_PAYOUT',
        'VIEW_PAYOUT',
        'SEND_MESSAGE',
        'VIEW_CONVERSATION',
        'UPLOAD_ATTACHMENT'
    )
ON CONFLICT DO NOTHING;

-- 3. Gán tất cả quyền cho role ADMIN
INSERT INTO
    role_capabilities (role_id, capability_id)
SELECT (
        SELECT id
        FROM roles
        WHERE
            name = 'ADMIN'
    ), id
FROM capabilities
ON CONFLICT DO NOTHING;