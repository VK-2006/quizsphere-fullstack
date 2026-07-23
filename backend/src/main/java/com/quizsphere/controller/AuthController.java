package com.quizsphere.controller;

import com.quizsphere.dto.*;
import com.quizsphere.entity.User;
import com.quizsphere.service.AccountRecoveryService;
import com.quizsphere.service.AuthService;
import com.quizsphere.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final CurrentUserService currentUserService;
    private final AccountRecoveryService accountRecoveryService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/google")
    public AuthResponse googleLogin(@Valid @RequestBody GoogleAuthRequest request) {
        return authService.googleLogin(request);
    }

    @PostMapping("/recovery-question")
    public RecoveryQuestionResponse recoveryQuestion(@Valid @RequestBody RecoveryQuestionRequest request) {
        return accountRecoveryService.getQuestion(request);
    }

    @PostMapping("/verify-security-answer")
    public RecoveryChallengeResponse verifySecurityAnswer(@Valid @RequestBody SecurityAnswerRequest request) {
        return accountRecoveryService.verifySecurityAnswer(request);
    }

    @PostMapping("/verify-recovery-code")
    public ResetTokenResponse verifyRecoveryCode(@Valid @RequestBody VerifyRecoveryCodeRequest request) {
        return accountRecoveryService.verifyRecoveryCode(request);
    }

    @PostMapping("/reset-security-question")
    public SecurityQuestionResetResponse resetSecurityQuestion(
            @Valid @RequestBody ResetSecurityQuestionRequest request) {
        return accountRecoveryService.resetSecurityQuestion(request);
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return accountRecoveryService.resetPassword(request);
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        User user = currentUserService.require(authentication);
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name(), user.isEnabled());
    }
}
