package com.quizsphere;

import com.quizsphere.dto.*;
import com.quizsphere.entity.AuthProvider;
import com.quizsphere.entity.Role;
import com.quizsphere.entity.User;
import com.quizsphere.repository.PasswordRecoverySessionRepository;
import com.quizsphere.repository.UserRepository;
import com.quizsphere.service.AccountRecoveryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AccountRecoveryFlowTests {
    @Autowired
    private AccountRecoveryService recoveryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordRecoverySessionRepository sessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void userCanVerifyAnswerAndRecoveryCodeThenResetPassword() {
        String email = "recovery-test@quizsphere.local";
        User user = User.builder()
                .fullName("Recovery Test")
                .email(email)
                .password(passwordEncoder.encode("OldPass@123"))
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .enabled(true)
                .build();
        String recoveryCode = recoveryService.configureNewUser(
                user,
                "What is your favorite color?",
                "Midnight Blue"
        );
        userRepository.save(user);

        RecoveryQuestionResponse question = recoveryService.getQuestion(new RecoveryQuestionRequest(email));
        assertThat(question.question()).isEqualTo("What is your favorite color?");

        RecoveryChallengeResponse challenge = recoveryService.verifySecurityAnswer(
                new SecurityAnswerRequest(email, "  midnight   blue ")
        );
        ResetTokenResponse reset = recoveryService.verifyRecoveryCode(
                new VerifyRecoveryCodeRequest(email, challenge.challengeToken(), recoveryCode)
        );
        recoveryService.resetPassword(new ResetPasswordRequest(
                reset.resetToken(), "NewPass@123", "NewPass@123"
        ));

        User updated = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        assertThat(passwordEncoder.matches("NewPass@123", updated.getPassword())).isTrue();
        assertThat(sessionRepository.findByResetTokenHashAndUsedFalse("unused")).isEmpty();
    }

    @Test
    void recoveryCodeCanReplaceForgottenSecurityQuestion() {
        String email = "question-reset@quizsphere.local";
        User user = User.builder()
                .fullName("Question Reset")
                .email(email)
                .password(passwordEncoder.encode("OldPass@123"))
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .enabled(true)
                .build();
        String oldRecoveryCode = recoveryService.configureNewUser(
                user,
                "What is your favorite place?",
                "Araku"
        );
        userRepository.save(user);

        SecurityQuestionResetResponse response = recoveryService.resetSecurityQuestion(
                new ResetSecurityQuestionRequest(
                        email,
                        oldRecoveryCode,
                        "What is your favorite food?",
                        "Biryani"
                )
        );

        assertThat(response.recoveryCode()).startsWith("QSR-");
        assertThat(response.recoveryCode()).isNotEqualTo(oldRecoveryCode);
        User updated = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        assertThat(updated.getSecurityQuestion()).isEqualTo("What is your favorite food?");
        assertThat(passwordEncoder.matches("biryani", updated.getSecurityAnswerHash())).isTrue();
    }
}
