package com.unishare.api.infrastructure.mail.impl;

import com.unishare.api.infrastructure.mail.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.time.Year;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Qualifier("mailTemplateEngine")
    private final SpringTemplateEngine mailTemplateEngine;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.brand-name:UniShare}")
    private String brandName;

    @Value("${app.mail.otp-valid-minutes:10}")
    private int linkValidMinutes;

    @Override
    public void sendEmailVerification(String to, String verificationLink) {
        String subject = "Xác minh tài khoản " + brandName + " của bạn";
        String body = renderVerifyEmailTemplate(subject, verificationLink);
        sendHtmlEmail(to, subject, body);
    }

    @Override
    public void sendPasswordReset(String to, String resetLink) {
        String subject = "Đặt lại mật khẩu " + brandName;
        String body = renderPasswordResetTemplate(subject, resetLink);
        sendHtmlEmail(to, subject, body);
    }

    @Override
    public void sendOrderPaidNotice(String toEmail, java.util.UUID orderId) {
        String subject = "[" + brandName + "] Thanh toán thành công — đơn " + orderId;
        String html = simpleCard(
                "Thanh toán thành công",
                "Đơn hàng <strong>" + orderId + "</strong> đã được thanh toán. Booking sẽ được tạo tự động — bạn sẽ nhận thêm email khi lịch học được xác nhận.");
        sendHtmlEmail(toEmail, subject, html);
    }

    @Override
    public void sendOrderPaymentFailedNotice(String toEmail, java.util.UUID orderId) {
        String subject = "[" + brandName + "] Thanh toán không thành công — đơn " + orderId;
        String html = simpleCard(
                "Thanh toán không thành công",
                "Đơn <strong>" + orderId + "</strong> chưa thanh toán. Bạn có thể thử lại từ ứng dụng.");
        sendHtmlEmail(toEmail, subject, html);
    }

    @Override
    public void sendBookingCreatedNotice(String buyerEmail, String mentorEmail, java.util.UUID bookingId, java.util.UUID orderId) {
        String base = "Booking <strong>" + bookingId + "</strong> (đơn " + orderId + ") đã được tạo.";
        if (buyerEmail != null && !buyerEmail.isBlank()) {
            sendHtmlEmail(buyerEmail, "[" + brandName + "] Đã tạo lịch học", simpleCard("Lịch học mới", base + " Mentor sẽ cập nhật buổi học."));
        }
        if (mentorEmail != null && !mentorEmail.isBlank()) {
            sendHtmlEmail(mentorEmail, "[" + brandName + "] Bạn có booking mới", simpleCard("Booking mới", base + " Vui lòng kiểm tra buổi học trong ứng dụng."));
        }
    }

    private String simpleCard(String title, String paragraphHtml) {
        return """
                <!DOCTYPE html><html><body style="font-family:Segoe UI,Roboto,sans-serif;background:#f1f5f9;padding:24px;">
                <div style="max-width:520px;margin:auto;background:#fff;border-radius:12px;padding:28px;border:1px solid #e2e8f0;">
                <h2 style="color:#4f46e5;margin:0 0 12px;">%s</h2>
                <p style="color:#334155;line-height:1.6;margin:0;">%s</p>
                <p style="color:#94a3b8;font-size:12px;margin-top:20px;">%s</p>
                </div></body></html>
                """.formatted(title, paragraphHtml, "© " + Year.now().getValue() + " " + brandName);
    }

    private String renderVerifyEmailTemplate(String pageTitle, String actionUrl) {
        return renderActionEmailTemplate(
                pageTitle,
                actionUrl,
                "Xác minh email",
                "Nhấn nút bên dưới để xác minh địa chỉ email của bạn.",
                "Không chia sẻ email này với người khác.",
                "Xác minh email");
    }

    private String renderPasswordResetTemplate(String pageTitle, String actionUrl) {
        return renderActionEmailTemplate(
                pageTitle,
                actionUrl,
                "Đặt lại mật khẩu",
                "Nhấn nút bên dưới để đặt lại mật khẩu. Nếu bạn không yêu cầu, hãy bỏ qua email này.",
                "Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.",
                "Đặt lại mật khẩu");
    }

    /** Template chung: {@code templates/mail/action-email.html} — chỉ khác nội dung qua biến. */
    private String renderActionEmailTemplate(
            String pageTitle,
            String actionUrl,
            String headline,
            String description,
            String validityExtra,
            String actionLabel) {
        Context ctx = new Context();
        ctx.setVariable("subject", pageTitle);
        ctx.setVariable("brandName", brandName);
        ctx.setVariable("headline", headline);
        ctx.setVariable("description", description);
        ctx.setVariable("validityExtra", validityExtra);
        ctx.setVariable("actionLabel", actionLabel);
        ctx.setVariable("actionUrl", actionUrl);
        ctx.setVariable("validMinutes", linkValidMinutes);
        ctx.setVariable("year", Year.now().getValue());
        return mailTemplateEngine.process("action-email", ctx);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("[Mail] Sent email to {}: {}", to, subject);
        } catch (MessagingException e) {
            log.error("[Mail] Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
