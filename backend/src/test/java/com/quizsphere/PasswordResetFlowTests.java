package com.quizsphere;

import com.quizsphere.dto.ForgotPasswordRequest;
import com.quizsphere.dto.ResetPasswordRequest;
import com.quizsphere.dto.ResetTokenResponse;
import com.quizsphere.dto.VerifyResetOtpRequest;
import com.quizsphere.entity.Role;
import com.quizsphere.entity.User;
import com.quizsphere.repository.PasswordResetOtpRepository;
import com.quizsphere.repository.UserRepository;
import com.quizsphere.service.EmailService;
import com.quizsphere.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
class PasswordResetFlowTests {
    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetOtpRepository otpRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private EmailService emailService;

    @Test
    void userCanRequestOtpVerifyItAndResetPassword() {
        String email = "reset-test@quizsphere.local";
        User user = userRepository.findByEmailIgnoreCase(email).orElseGet(() -> userRepository.save(User.builder()
                .fullName("Reset Test")
                .email(email)
                .password(passwordEncoder.encode("OldPass@123"))
                .role(Role.USER)
                .enabled(true)
                .build()));
        otpRepository.deleteByUser(user);

        passwordResetService.requestOtp(new ForgotPasswordRequest(email));

        ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendPasswordResetOtp(eq(email), eq("Reset Test"), otpCaptor.capture(), eq(10L));
        String otp = otpCaptor.getValue();
        assertThat(otp).matches("\\d{6}");

        ResetTokenResponse verified = passwordResetService.verifyOtp(new VerifyResetOtpRequest(email, otp));
        passwordResetService.resetPassword(new ResetPasswordRequest(
                verified.resetToken(), "NewPass@123", "NewPass@123"));

        User updated = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        assertThat(passwordEncoder.matches("NewPass@123", updated.getPassword())).isTrue();
        assertThat(otpRepository.findFirstByUserAndUsedFalseOrderByCreatedAtDesc(updated)).isEmpty();
    }
}
