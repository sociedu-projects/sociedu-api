package com.unishare.api.common.event;

/**
 * Gửi email xác minh — liên kết một lần, xử lý async sau khi transaction commit.
 */
public record EmailVerificationMailEvent(String toEmail, String verificationLink) implements DomainEvent {}
