package com.quizsphere.service;

import com.quizsphere.dto.*;
import com.quizsphere.entity.AuthProvider;
import com.quizsphere.entity.PasswordResetOtp;
import com.quizsphere.entity.User;
import com.quizsphere.exception.BadRequestException;
import com.quizsphere.repository.PasswordResetOtpRepository;
import com.quizsphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private static final String GENERIC_SENT_MESSAGE =
            "If an account exists for that email, a 6-digit OTP has been sent.";

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.password-reset.otp-expiry-minutes:10}")
    private long otpExpiryMinutes;

    @Value("${app.password-reset.reset-token-expiry-minutes:10}")
    private long resetTokenExpiryMinutes;

    @Value("${app.password-reset.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    @Value("${app.password-reset.max-attempts:5}")
    private int maxAttempts;

    @Transactional
    public MessageResponse requestOtp(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.email());
        Optional<User> optionalUser = userRepository.findByEmailIgnoreCase(email);
        if (optionalUser.isEmpty()) {
            return new MessageResponse(GENERIC_SENT_MESSAGE);
        }

        User user = optionalUser.get();
        Instant now = Instant.now();
        otpRepository.findFirstByUserAndUsedFalseOrderByCreatedAtDesc(user).ifPresent(existing -> {
            if (existing.getCreatedAt() != null) {
                long secondsSinceCreation = Duration.between(existing.getCreatedAt(), now).getSeconds();
                if (secondsSinceCreation < resendCooldownSeconds) {
                    long wait = Math.max(1, resendCooldownSeconds - secondsSinceCreation);
                    throw new BadRequestException("Please wait " + wait + " seconds before requesting another OTP");
                }
            }
        });

        otpRepository.deleteByUser(user);
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        PasswordResetOtp reset = otpRepository.saveAndFlush(PasswordResetOtp.builder()
                .user(user)
                .otpHash(passwordEncoder.encode(otp))
                .expiresAt(now.plusSeconds(otpExpiryMinutes * 60))
                .build());

        try {
            emailService.sendPasswordResetOtp(user.getEmail(), user.getFullName(), otp, otpExpiryMinutes);
        } catch (RuntimeException ex) {
            otpRepository.delete(reset);
            throw ex;
        }
        return new MessageResponse(GENERIC_SENT_MESSAGE);
    }

    @Transactional
    public ResetTokenResponse verifyOtp(VerifyResetOtpRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadRequestException("Invalid or expired OTP"));
        PasswordResetOtp reset = otpRepository.findFirstByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new BadRequestException("Invalid or expired OTP"));

        Instant now = Instant.now();
        if (reset.isUsed() || now.isAfter(reset.getExpiresAt())) {
            throw new BadRequestException("OTP has expired. Request a new OTP.");
        }
        if (reset.getAttempts() >= maxAttempts) {
            throw new BadRequestException("Too many incorrect attempts. Request a new OTP.");
        }
        if (!passwordEncoder.matches(request.otp(), reset.getOtpHash())) {
            reset.setAttempts(reset.getAttempts() + 1);
            otpRepository.save(reset);
            int remaining = Math.max(0, maxAttempts - reset.getAttempts());
            throw new BadRequestException("Incorrect OTP. " + remaining + " attempt(s) remaining.");
        }

        String resetToken = generateSecureToken();
        reset.setVerified(true);
        reset.setResetTokenHash(sha256(resetToken));
        reset.setResetTokenExpiresAt(now.plusSeconds(resetTokenExpiryMinutes * 60));
        otpRepository.save(reset);
        return new ResetTokenResponse("OTP verified. You can now create a new password.",
                resetToken, resetTokenExpiryMinutes);
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BadRequestException("New password and confirmation do not match");
        }
        String tokenHash = sha256(request.resetToken());
        PasswordResetOtp reset = otpRepository.findByResetTokenHashAndUsedFalse(tokenHash)
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset session"));

        Instant now = Instant.now();
        if (!reset.isVerified() || reset.getResetTokenExpiresAt() == null ||
                now.isAfter(reset.getResetTokenExpiresAt())) {
            throw new BadRequestException("Password reset session has expired. Start again.");
        }

        User user = reset.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        if (user.getAuthProvider() == AuthProvider.GOOGLE) {
            user.setAuthProvider(AuthProvider.BOTH);
        }
        userRepository.save(user);
        reset.setUsed(true);
        otpRepository.save(reset);
        otpRepository.deleteByUser(user);
        return new MessageResponse("Password reset successful. Sign in with your new password.");
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return java.util.HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
