package com.unishare.api.common.event;

/** Gửi thông báo thanh toán thành công (tách khỏi {@link OrderPaidEvent} để SMTP chạy async). */
public record OrderPaidNotificationMailEvent(String toEmail, java.util.UUID orderId) implements DomainEvent {}
