package com.quizsphere.repository;

import com.quizsphere.entity.PasswordResetOtp;
import com.quizsphere.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {
    Optional<PasswordResetOtp> findFirstByUserAndUsedFalseOrderByCreatedAtDesc(User user);
    Optional<PasswordResetOtp> findByResetTokenHashAndUsedFalse(String resetTokenHash);
    void deleteByUser(User user);
}
