package com.unishare.api.infrastructure.mail;

/**
 * Mail service interface for sending transactional emails.
 * Implementations must be non-blocking (async).
 */
public interface MailService {

    void sendEmailVerification(String to, String otp);

    void sendPasswordReset(String to, String otp);
}
