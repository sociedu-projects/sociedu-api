package com.unishare.api.common.event;

/** Gửi thông báo booking tạo (buyer/mentor có thể null). */
public record BookingCreatedNotificationMailEvent(
        String buyerEmail,
        String mentorEmail,
        java.util.UUID bookingId,
        java.util.UUID orderId)
        implements DomainEvent {}
