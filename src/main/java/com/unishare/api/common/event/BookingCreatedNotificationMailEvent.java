package com.unishare.api.common.event;

/** Gửi thông báo booking tạo (buyer/mentor có thể null). */
public record BookingCreatedNotificationMailEvent(
        String buyerEmail,
        String mentorEmail,
        long bookingId,
        long orderId)
        implements DomainEvent {}
