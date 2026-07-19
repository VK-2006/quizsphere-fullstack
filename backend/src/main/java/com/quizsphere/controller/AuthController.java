package com.quizsphere.controller;

import com.quizsphere.dto.*;
import com.quizsphere.entity.User;
import com.quizsphere.service.AuthService;
import com.quizsphere.service.CurrentUserService;
import com.quizsphere.service.PasswordResetService;
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
    private final PasswordResetService passwordResetService;

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

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return passwordResetService.requestOtp(request);
    }

    @PostMapping("/verify-reset-otp")
    public ResetTokenResponse verifyResetOtp(@Valid @RequestBody VerifyResetOtpRequest request) {
        return passwordResetService.verifyOtp(request);
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return passwordResetService.resetPassword(request);
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        User user = currentUserService.require(authentication);
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name(), user.isEnabled());
    }
}
