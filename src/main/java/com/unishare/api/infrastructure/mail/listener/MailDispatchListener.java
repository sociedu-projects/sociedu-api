package com.unishare.api.infrastructure.mail.listener;

import com.unishare.api.common.event.BookingCreatedNotificationMailEvent;
import com.unishare.api.common.event.EmailVerificationMailEvent;
import com.unishare.api.common.event.MentorApprovedNotificationMailEvent;
import com.unishare.api.common.event.MentorRejectedNotificationMailEvent;
import com.unishare.api.common.event.OrderPaidNotificationMailEvent;
import com.unishare.api.common.event.OrderPaymentFailedNotificationMailEvent;
import com.unishare.api.common.event.PasswordResetMailEvent;
import com.unishare.api.infrastructure.mail.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Điểm duy nhất gọi {@link MailService} — luôn qua event + async để không block request/transaction.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MailDispatchListener {

    private final MailService mailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onEmailVerificationMail(EmailVerificationMailEvent event) {
        mailService.sendEmailVerification(event.toEmail(), event.verificationLink());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onPasswordResetMail(PasswordResetMailEvent event) {
        mailService.sendPasswordReset(event.toEmail(), event.resetLink());
    }

    @EventListener
    @Async
    public void onOrderPaidNotification(OrderPaidNotificationMailEvent event) {
        mailService.sendOrderPaidNotice(event.toEmail(), event.orderId());
    }

    @EventListener
    @Async
    public void onOrderPaymentFailedNotification(OrderPaymentFailedNotificationMailEvent event) {
        mailService.sendOrderPaymentFailedNotice(event.toEmail(), event.orderId());
    }

    @EventListener
    @Async
    public void onBookingCreatedNotification(BookingCreatedNotificationMailEvent event) {
        mailService.sendBookingCreatedNotice(
                event.buyerEmail(), event.mentorEmail(), event.bookingId(), event.orderId());
    }

    @EventListener
    @Async
    public void onMentorApprovedNotification(MentorApprovedNotificationMailEvent event) {
        mailService.sendMentorApprovedNotice(event.toEmail(), event.requestId());
    }

    @EventListener
    @Async
    public void onMentorRejectedNotification(MentorRejectedNotificationMailEvent event) {
        mailService.sendMentorRejectedNotice(event.toEmail(), event.requestId(), event.reason());
    }
}
