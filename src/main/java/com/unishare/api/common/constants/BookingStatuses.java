package com.unishare.api.common.constants;

/** Khớp enum {@code booking_status} trong schema. */
public final class BookingStatuses {

    private BookingStatuses() {}

    public static final String PENDING = "pending";
    public static final String SCHEDULED = "scheduled";
    public static final String IN_PROGRESS = "in_progress";
    public static final String COMPLETED = "completed";
    public static final String CANCELED = "canceled";
    public static final String REFUNDED = "refunded";
}
