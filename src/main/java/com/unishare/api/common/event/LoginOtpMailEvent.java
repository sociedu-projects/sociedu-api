package com.unishare.api.common.event;

/**
 * Gửi OTP đăng nhập qua email — xử lý async sau khi transaction commit.
 */
public record LoginOtpMailEvent(String toEmail, String otpCode) implements DomainEvent {}
