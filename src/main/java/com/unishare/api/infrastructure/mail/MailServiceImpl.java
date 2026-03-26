package com.unishare.api.infrastructure.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Async
    @Override
    public void sendEmailVerification(String to, String otp) {
        String subject = "Xác minh tài khoản UniShare của bạn";
        String body = buildOtpEmailBody("Xác minh Email", otp,
                "Sử dụng mã OTP dưới đây để xác minh địa chỉ email của bạn. Mã có hiệu lực trong 10 phút.");
        sendHtmlEmail(to, subject, body);
    }

    @Async
    @Override
    public void sendPasswordReset(String to, String otp) {
        String subject = "Đặt lại mật khẩu UniShare";
        String body = buildOtpEmailBody("Đặt lại Mật khẩu", otp,
                "Sử dụng mã OTP dưới đây để đặt lại mật khẩu của bạn. Mã có hiệu lực trong 10 phút.");
        sendHtmlEmail(to, subject, body);
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

    private String buildOtpEmailBody(String title, String otp, String description) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                  <div style="max-width: 480px; margin: auto; background: #fff; border-radius: 8px; padding: 32px;">
                    <h2 style="color: #4F46E5;">UniShare — %s</h2>
                    <p style="color: #555;">%s</p>
                    <div style="text-align: center; margin: 24px 0;">
                      <span style="font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #4F46E5;">%s</span>
                    </div>
                    <p style="color: #999; font-size: 12px;">Nếu bạn không thực hiện yêu cầu này, hãy bỏ qua email này.</p>
                  </div>
                </body>
                </html>
                """.formatted(title, description, otp);
    }
}
