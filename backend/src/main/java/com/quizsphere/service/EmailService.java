package com.quizsphere.service;

import com.quizsphere.exception.EmailDeliveryException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${app.mail.from:}")
    private String configuredFrom;

    public void sendPasswordResetOtp(String recipient, String fullName, String otp, long expiryMinutes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            String from = configuredFrom == null || configuredFrom.isBlank() ? mailUsername : configuredFrom;
            if (from == null || from.isBlank()) {
                throw new EmailDeliveryException("Email sender is not configured. Set MAIL_USERNAME and MAIL_APP_PASSWORD.", null);
            }

            helper.setFrom(from);
            helper.setTo(recipient);
            helper.setSubject("QuizSphere password reset OTP");
            helper.setText(buildOtpEmail(fullName, otp, expiryMinutes), true);
            mailSender.send(message);
        } catch (MessagingException | MailException ex) {
            throw new EmailDeliveryException("Unable to send the OTP email. Check the mail configuration and try again.", ex);
        }
    }

    private String buildOtpEmail(String fullName, String otp, long expiryMinutes) {
        String safeName = escapeHtml(fullName == null || fullName.isBlank() ? "QuizSphere user" : fullName);
        return """
                <!doctype html>
                <html>
                <body style="margin:0;background:#f5f6fb;font-family:Arial,sans-serif;color:#15213a">
                  <div style="max-width:560px;margin:32px auto;background:#ffffff;border:1px solid #e5e8f1;border-radius:20px;padding:32px">
                    <div style="font-size:13px;font-weight:700;letter-spacing:2px;color:#6259e8">QUIZSPHERE</div>
                    <h2 style="margin:14px 0 8px">Reset your password</h2>
                    <p>Hello %s,</p>
                    <p>Use the following one-time password to continue resetting your QuizSphere password:</p>
                    <div style="font-size:34px;font-weight:800;letter-spacing:10px;text-align:center;background:#f3f1ff;color:#4a42c8;border-radius:14px;padding:20px;margin:24px 0">%s</div>
                    <p>This OTP expires in <strong>%d minutes</strong>. Do not share it with anyone.</p>
                    <p style="color:#6e7890;font-size:13px;margin-top:28px">If you did not request this reset, you can safely ignore this email.</p>
                  </div>
                </body>
                </html>
                """.formatted(safeName, otp, expiryMinutes);
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
