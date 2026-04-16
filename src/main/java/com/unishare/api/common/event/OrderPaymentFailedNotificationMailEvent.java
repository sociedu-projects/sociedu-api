package com.unishare.api.common.event;

/** Gửi thông báo thanh toán thất bại (async). */
public record OrderPaymentFailedNotificationMailEvent(String toEmail, java.util.UUID orderId)
        implements DomainEvent {}
