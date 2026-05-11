package com.unishare.api.common.event;

/**
 * Gửi OTP qua email để xác thực số điện thoại — xử lý async sau khi transaction commit.
 */
public record PhoneVerificationOtpMailEvent(String toEmail, String otpCode) implements DomainEvent {}
