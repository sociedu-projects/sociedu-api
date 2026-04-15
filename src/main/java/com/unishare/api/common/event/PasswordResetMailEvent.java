package com.unishare.api.common.event;

/**
 * Gửi email đặt lại mật khẩu — liên kết một lần, xử lý async sau khi transaction commit.
 */
public record PasswordResetMailEvent(String toEmail, String resetLink) implements DomainEvent {}
