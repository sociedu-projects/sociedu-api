package com.unishare.api.common.event;

/** Gửi thông báo thanh toán thất bại (async). */
public record OrderPaymentFailedNotificationMailEvent(String toEmail, long orderId)
        implements DomainEvent {}
