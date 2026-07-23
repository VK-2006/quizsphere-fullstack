package com.quizsphere.service;

import com.quizsphere.dto.*;
import com.quizsphere.entity.AuthProvider;
import com.quizsphere.entity.PasswordRecoverySession;
import com.quizsphere.entity.User;
import com.quizsphere.exception.BadRequestException;
import com.quizsphere.repository.PasswordRecoverySessionRepository;
import com.quizsphere.repository.UserRepository;
import com.quizsphere.security.SecurityQuestionCatalog;
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
import java.util.HexFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AccountRecoveryService {
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final UserRepository userRepository;
    private final PasswordRecoverySessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.password-recovery.challenge-expiry-minutes:10}")
    private long challengeExpiryMinutes;

    @Value("${app.password-recovery.reset-token-expiry-minutes:10}")
    private long resetTokenExpiryMinutes;

    @Value("${app.password-recovery.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.password-recovery.lock-minutes:15}")
    private long lockMinutes;

    public String configureNewUser(User user, String securityQuestion, String securityAnswer) {
        String recoveryCode = generateRecoveryCode();
        applyRecoverySettings(user, securityQuestion, securityAnswer, recoveryCode);
        return recoveryCode;
    }

    @Transactional(readOnly = true)
    public RecoveryQuestionResponse getQuestion(RecoveryQuestionRequest request) {
        User user = requireRecoverableUser(request.email());
        ensureRecoveryConfigured(user);
        return new RecoveryQuestionResponse(user.getSecurityQuestion(), true);
    }

    @Transactional(noRollbackFor = BadRequestException.class)
    public RecoveryChallengeResponse verifySecurityAnswer(SecurityAnswerRequest request) {
        User user = requireRecoverableUser(request.email());
        ensureRecoveryConfigured(user);
        ensureNotLocked(user);

        if (!passwordEncoder.matches(normalizeAnswer(request.securityAnswer()), user.getSecurityAnswerHash())) {
            recordFailure(user, "Incorrect security answer");
        }

        clearFailures(user);
        sessionRepository.deleteByUser(user);
        String challengeToken = generateSecureToken();
        Instant now = Instant.now();
        sessionRepository.save(PasswordRecoverySession.builder()
                .user(user)
                .challengeTokenHash(sha256(challengeToken))
                .challengeExpiresAt(now.plusSeconds(challengeExpiryMinutes * 60))
                .answerVerified(true)
                .build());

        return new RecoveryChallengeResponse(
                "Security answer verified. Enter your recovery code.",
                challengeToken,
                challengeExpiryMinutes
        );
    }

    @Transactional(noRollbackFor = BadRequestException.class)
    public ResetTokenResponse verifyRecoveryCode(VerifyRecoveryCodeRequest request) {
        User user = requireRecoverableUser(request.email());
        ensureRecoveryConfigured(user);
        ensureNotLocked(user);

        PasswordRecoverySession session = sessionRepository
                .findByUserAndChallengeTokenHashAndUsedFalse(user, sha256(request.challengeToken()))
                .orElseThrow(() -> new BadRequestException("Invalid or expired recovery session"));

        Instant now = Instant.now();
        if (!session.isAnswerVerified() || session.getChallengeExpiresAt() == null ||
                now.isAfter(session.getChallengeExpiresAt())) {
            throw new BadRequestException("Recovery session expired. Start again.");
        }

        if (!passwordEncoder.matches(normalizeRecoveryCode(request.recoveryCode()), user.getRecoveryCodeHash())) {
            recordFailure(user, "Incorrect recovery code");
        }

        clearFailures(user);
        String resetToken = generateSecureToken();
        session.setRecoveryCodeVerified(true);
        session.setResetTokenHash(sha256(resetToken));
        session.setResetTokenExpiresAt(now.plusSeconds(resetTokenExpiryMinutes * 60));
        sessionRepository.save(session);

        return new ResetTokenResponse(
                "Recovery code verified. Create your new password.",
                resetToken,
                resetTokenExpiryMinutes
        );
    }

    @Transactional(noRollbackFor = BadRequestException.class)
    public SecurityQuestionResetResponse resetSecurityQuestion(ResetSecurityQuestionRequest request) {
        User user = requireRecoverableUser(request.email());
        ensureRecoveryConfigured(user);
        ensureNotLocked(user);

        if (!passwordEncoder.matches(normalizeRecoveryCode(request.recoveryCode()), user.getRecoveryCodeHash())) {
            recordFailure(user, "Incorrect recovery code");
        }

        String newRecoveryCode = generateRecoveryCode();
        applyRecoverySettings(user, request.securityQuestion(), request.securityAnswer(), newRecoveryCode);
        userRepository.save(user);
        sessionRepository.deleteByUser(user);

        String resetToken = generateSecureToken();
        Instant now = Instant.now();
        sessionRepository.save(PasswordRecoverySession.builder()
                .user(user)
                .answerVerified(true)
                .recoveryCodeVerified(true)
                .resetTokenHash(sha256(resetToken))
                .resetTokenExpiresAt(now.plusSeconds(resetTokenExpiryMinutes * 60))
                .build());

        return new SecurityQuestionResetResponse(
                "Security question updated. Save the new recovery code, then create a new password.",
                resetToken,
                resetTokenExpiryMinutes,
                newRecoveryCode
        );
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BadRequestException("New password and confirmation do not match");
        }

        PasswordRecoverySession session = sessionRepository.findByResetTokenHashAndUsedFalse(sha256(request.resetToken()))
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset session"));

        Instant now = Instant.now();
        if (!session.isRecoveryCodeVerified() || session.getResetTokenExpiresAt() == null ||
                now.isAfter(session.getResetTokenExpiresAt())) {
            throw new BadRequestException("Password reset session expired. Start again.");
        }

        User user = session.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        if (user.getAuthProvider() == AuthProvider.GOOGLE) {
            user.setAuthProvider(AuthProvider.BOTH);
        }
        userRepository.save(user);
        session.setUsed(true);
        sessionRepository.save(session);
        sessionRepository.deleteByUser(user);
        return new MessageResponse("Password reset successful. Sign in with your new password.");
    }

    @Transactional(readOnly = true)
    public RecoverySettingsResponse getRecoverySettings(User user) {
        boolean configured = isConfigured(user);
        return new RecoverySettingsResponse(
                configured,
                configured ? user.getSecurityQuestion() : null,
                null,
                configured ? "Account recovery is configured." : "Set up account recovery to enable password reset."
        );
    }

    @Transactional
    public RecoverySettingsResponse updateRecoverySettings(User user, RecoverySettingsRequest request) {
        if (user.getAuthProvider() != AuthProvider.GOOGLE) {
            if (request.currentPassword() == null || request.currentPassword().isBlank() ||
                    !passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
                throw new BadRequestException("Current password is incorrect");
            }
        }

        String recoveryCode = generateRecoveryCode();
        applyRecoverySettings(user, request.securityQuestion(), request.securityAnswer(), recoveryCode);
        userRepository.save(user);
        sessionRepository.deleteByUser(user);

        return new RecoverySettingsResponse(
                true,
                user.getSecurityQuestion(),
                recoveryCode,
                "Account recovery settings updated. Save the new recovery code now."
        );
    }

    private User requireRecoverableUser(String email) {
        return userRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .filter(User::isEnabled)
                .orElseThrow(() -> new BadRequestException("Account recovery is unavailable for this account"));
    }

    private void ensureRecoveryConfigured(User user) {
        if (!isConfigured(user)) {
            throw new BadRequestException(
                    "Account recovery is not configured. Sign in and configure it from Profile settings."
            );
        }
    }

    private boolean isConfigured(User user) {
        return user.getSecurityQuestion() != null && !user.getSecurityQuestion().isBlank() &&
                user.getSecurityAnswerHash() != null && !user.getSecurityAnswerHash().isBlank() &&
                user.getRecoveryCodeHash() != null && !user.getRecoveryCodeHash().isBlank();
    }

    private void ensureNotLocked(User user) {
        Instant lockedUntil = user.getRecoveryLockedUntil();
        if (lockedUntil == null) return;

        Instant now = Instant.now();
        if (now.isBefore(lockedUntil)) {
            long minutes = Math.max(1, Duration.between(now, lockedUntil).toMinutes() + 1);
            throw new BadRequestException("Too many failed recovery attempts. Try again in " + minutes + " minute(s).");
        }

        clearFailures(user);
    }

    private void recordFailure(User user, String prefix) {
        int attempts = user.getRecoveryFailedAttempts() + 1;
        user.setRecoveryFailedAttempts(attempts);
        int remaining = Math.max(0, maxAttempts - attempts);

        if (attempts >= maxAttempts) {
            user.setRecoveryLockedUntil(Instant.now().plusSeconds(lockMinutes * 60));
            userRepository.save(user);
            throw new BadRequestException(prefix + ". Account recovery is locked for " + lockMinutes + " minute(s).");
        }

        userRepository.save(user);
        throw new BadRequestException(prefix + ". " + remaining + " attempt(s) remaining.");
    }

    private void clearFailures(User user) {
        user.setRecoveryFailedAttempts(0);
        user.setRecoveryLockedUntil(null);
        userRepository.save(user);
    }

    private void applyRecoverySettings(User user, String question, String answer, String recoveryCode) {
        user.setSecurityQuestion(SecurityQuestionCatalog.validate(question));
        user.setSecurityAnswerHash(passwordEncoder.encode(normalizeAnswer(answer)));
        user.setRecoveryCodeHash(passwordEncoder.encode(normalizeRecoveryCode(recoveryCode)));
        user.setSecurityQuestionUpdatedAt(Instant.now());
        user.setRecoveryFailedAttempts(0);
        user.setRecoveryLockedUntil(null);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeAnswer(String answer) {
        return answer.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private String normalizeRecoveryCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    }

    private String generateRecoveryCode() {
        StringBuilder value = new StringBuilder("QSR-");
        for (int group = 0; group < 3; group++) {
            if (group > 0) value.append('-');
            for (int index = 0; index < 4; index++) {
                value.append(CODE_ALPHABET.charAt(secureRandom.nextInt(CODE_ALPHABET.length())));
            }
        }
        return value.toString();
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
