package com.unishare.api.common.constants;

/**
 * Giá trị cột {@code capabilities.name} — khớp {@code init/data.sql} / Flyway.
 */
public final class Capabilities {

    private Capabilities() {}

    public static final String UPDATE_PROFILE = "UPDATE_PROFILE";
    public static final String VIEW_PROFILE = "VIEW_PROFILE";
    public static final String BOOK_SESSION = "BOOK_SESSION";
    public static final String CANCEL_BOOKING = "CANCEL_BOOKING";
    public static final String WRITE_REVIEW = "WRITE_REVIEW";

    public static final String CREATE_SERVICE_PACKAGE = "CREATE_SERVICE_PACKAGE";
    public static final String UPDATE_OWN_SERVICE_PACKAGE = "UPDATE_OWN_SERVICE_PACKAGE";
    public static final String DELETE_OWN_SERVICE_PACKAGE = "DELETE_OWN_SERVICE_PACKAGE";
    public static final String MANAGE_PACKAGE_CURRICULUM = "MANAGE_PACKAGE_CURRICULUM";
    public static final String VIEW_OWN_BOOKINGS = "VIEW_OWN_BOOKINGS";
    public static final String MANAGE_OWN_BOOKINGS = "MANAGE_OWN_BOOKINGS";
    public static final String MANAGE_SESSIONS = "MANAGE_SESSIONS";
    public static final String VIEW_EARNINGS = "VIEW_EARNINGS";

    public static final String VIEW_BOOKING = "VIEW_BOOKING";
    public static final String JOIN_SESSION = "JOIN_SESSION";
    public static final String START_SESSION = "START_SESSION";
    public static final String COMPLETE_SESSION = "COMPLETE_SESSION";

    public static final String CREATE_PAYMENT = "CREATE_PAYMENT";
    public static final String VIEW_PAYMENT = "VIEW_PAYMENT";
    public static final String REQUEST_PAYOUT = "REQUEST_PAYOUT";
    public static final String VIEW_PAYOUT = "VIEW_PAYOUT";
    public static final String REFUND_REQUEST = "REFUND_REQUEST";

    public static final String SEND_MESSAGE = "SEND_MESSAGE";
    public static final String VIEW_CONVERSATION = "VIEW_CONVERSATION";
    public static final String UPLOAD_ATTACHMENT = "UPLOAD_ATTACHMENT";

    public static final String CREATE_REPORT = "CREATE_REPORT";
    public static final String VIEW_OWN_REPORT = "VIEW_OWN_REPORT";
    public static final String CREATE_DISPUTE = "CREATE_DISPUTE";
    public static final String VIEW_OWN_DISPUTE = "VIEW_OWN_DISPUTE";

    public static final String MANAGE_USERS = "MANAGE_USERS";
    public static final String MANAGE_MENTORS = "MANAGE_MENTORS";
    public static final String MANAGE_ALL_BOOKINGS = "MANAGE_ALL_BOOKINGS";
    public static final String MANAGE_PAYMENTS = "MANAGE_PAYMENTS";
    public static final String RESOLVE_REPORT = "RESOLVE_REPORT";
    public static final String RESOLVE_DISPUTE = "RESOLVE_DISPUTE";
    public static final String VIEW_SYSTEM_METRICS = "VIEW_SYSTEM_METRICS";
    public static final String MANAGE_ALL = "MANAGE_ALL";
}
