package com.quizsphere.repository;

import com.quizsphere.entity.PasswordRecoverySession;
import com.quizsphere.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordRecoverySessionRepository extends JpaRepository<PasswordRecoverySession, Long> {
    Optional<PasswordRecoverySession> findByUserAndChallengeTokenHashAndUsedFalse(User user, String challengeTokenHash);
    Optional<PasswordRecoverySession> findByResetTokenHashAndUsedFalse(String resetTokenHash);
    void deleteByUser(User user);
}
