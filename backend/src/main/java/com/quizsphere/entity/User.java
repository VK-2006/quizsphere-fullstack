package com.quizsphere.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_users_google_subject", columnNames = "google_subject")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", length = 20)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(name = "google_subject", length = 255)
    private String googleSubject;

    @Column(name = "avatar_url", length = 1000)
    private String avatarUrl;

    @Column(length = 500)
    private String bio;

    @Column(length = 30)
    private String phone;

    @Column(length = 120)
    private String location;

    private LocalDate dateOfBirth;

    @Column(name = "security_question", length = 255)
    private String securityQuestion;

    @Column(name = "security_answer_hash", length = 100)
    private String securityAnswerHash;

    @Column(name = "recovery_code_hash", length = 100)
    private String recoveryCodeHash;

    @Column(name = "recovery_failed_attempts", nullable = false, columnDefinition = "INT NOT NULL DEFAULT 0")
    @Builder.Default
    private int recoveryFailedAttempts = 0;

    @Column(name = "recovery_locked_until")
    private Instant recoveryLockedUntil;

    @Column(name = "security_question_updated_at")
    private Instant securityQuestionUpdatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @PostLoad
    @PrePersist
    @PreUpdate
    private void ensureDefaults() {
        if (authProvider == null) authProvider = AuthProvider.LOCAL;
    }
}

