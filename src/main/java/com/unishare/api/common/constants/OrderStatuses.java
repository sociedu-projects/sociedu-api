package com.unishare.api.common.constants;

/** Khớp enum {@code order_status} trong schema. */
public final class OrderStatuses {

    private OrderStatuses() {}

    public static final String PENDING_PAYMENT = "pending_payment";
    public static final String PAID = "paid";
    public static final String FAILED = "failed";
    public static final String CANCELED = "canceled";
    public static final String REFUNDED = "refunded";
}
