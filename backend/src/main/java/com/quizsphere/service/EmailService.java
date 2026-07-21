package com.quizsphere.service;

import com.quizsphere.exception.EmailDeliveryException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {
    private final RestClient resendClient;

    @Value("${app.resend.api-key:}")
    private String resendApiKey;

    @Value("${app.resend.from:QuizSphere <onboarding@resend.dev>}")
    private String configuredFrom;

    @Value("${app.resend.reply-to:}")
    private String replyTo;

    public EmailService(RestClient.Builder restClientBuilder) {
        this.resendClient = restClientBuilder
                .baseUrl("https://api.resend.com")
                .build();
    }

    public void sendPasswordResetOtp(String recipient, String fullName, String otp, long expiryMinutes) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            throw new EmailDeliveryException(
                    "Resend is not configured. Set RESEND_API_KEY in the backend environment variables.",
                    null
            );
        }
        if (configuredFrom == null || configuredFrom.isBlank()) {
            throw new EmailDeliveryException(
                    "Resend sender is not configured. Set RESEND_FROM in the backend environment variables.",
                    null
            );
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("from", configuredFrom);
        payload.put("to", List.of(recipient));
        payload.put("subject", "QuizSphere password reset OTP");
        payload.put("html", buildOtpEmail(fullName, otp, expiryMinutes));
        if (replyTo != null && !replyTo.isBlank()) {
            payload.put("reply_to", replyTo);
        }

        try {
            resendClient.post()
                    .uri("/emails")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + resendApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new EmailDeliveryException(
                    "Unable to send the OTP email through Resend. Check RESEND_API_KEY, RESEND_FROM, and domain verification.",
                    ex
            );
        } catch (ResourceAccessException ex) {
            throw new EmailDeliveryException(
                    "Unable to reach the Resend email service. Try again shortly.",
                    ex
            );
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
