package com.unishare.api.infrastructure.mail;

/**
 * Gửi email SMTP — chỉ được gọi từ {@link com.unishare.api.infrastructure.mail.listener.MailDispatchListener}
 * (sau event, async) để request không chờ mail server.
 */
public interface MailService {

    void sendEmailVerification(String to, String verificationLink);

    void sendPasswordReset(String to, String resetLink);

    /** Trigger: {@link com.unishare.api.common.event.OrderPaidNotificationMailEvent}. */
    void sendOrderPaidNotice(String toEmail, java.util.UUID orderId);

    /** Trigger: {@link com.unishare.api.common.event.OrderPaymentFailedNotificationMailEvent}. */
    void sendOrderPaymentFailedNotice(String toEmail, java.util.UUID orderId);

    /** Trigger: {@link com.unishare.api.common.event.BookingCreatedNotificationMailEvent}. */
    void sendBookingCreatedNotice(String buyerEmail, String mentorEmail, java.util.UUID bookingId, java.util.UUID orderId);
}
