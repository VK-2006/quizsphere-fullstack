package com.quizsphere.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "password_recovery_sessions", indexes = {
        @Index(name = "idx_recovery_session_user", columnList = "user_id"),
        @Index(name = "idx_recovery_challenge_hash", columnList = "challenge_token_hash"),
        @Index(name = "idx_recovery_reset_hash", columnList = "reset_token_hash")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordRecoverySession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "challenge_token_hash", length = 64, unique = true)
    private String challengeTokenHash;

    @Column(name = "challenge_expires_at")
    private Instant challengeExpiresAt;

    @Column(name = "answer_verified", nullable = false)
    @Builder.Default
    private boolean answerVerified = false;

    @Column(name = "recovery_code_verified", nullable = false)
    @Builder.Default
    private boolean recoveryCodeVerified = false;

    @Column(name = "reset_token_hash", length = 64, unique = true)
    private String resetTokenHash;

    @Column(name = "reset_token_expires_at")
    private Instant resetTokenExpiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean used = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
