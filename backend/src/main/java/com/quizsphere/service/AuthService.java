package com.quizsphere.service;

import com.quizsphere.dto.*;
import com.quizsphere.entity.AuthProvider;
import com.quizsphere.entity.Role;
import com.quizsphere.entity.User;
import com.quizsphere.exception.BadRequestException;
import com.quizsphere.repository.UserRepository;
import com.quizsphere.security.CustomUserDetailsService;
import com.quizsphere.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final GoogleTokenService googleTokenService;
    private final AccountRecoveryService accountRecoveryService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("An account already exists with this email");
        }

        User user = User.builder()
                .fullName(request.fullName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .enabled(true)
                .build();

        String recoveryCode = accountRecoveryService.configureNewUser(
                user,
                request.securityQuestion(),
                request.securityAnswer()
        );
        user = userRepository.save(user);
        return tokenResponse(user, recoveryCode);
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));
        if (user.getAuthProvider() == AuthProvider.GOOGLE) {
            throw new BadRequestException("This account uses Google Sign-In. Continue with Google or set a password using account recovery.");
        }
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        return tokenResponse(user, null);
    }

    @Transactional
    public AuthResponse googleLogin(GoogleAuthRequest request) {
        GoogleTokenService.GoogleProfile google = googleTokenService.verify(request.credential());
        User user = userRepository.findByEmailIgnoreCase(google.email()).orElse(null);

        if (user == null) {
            String safeName = google.fullName() == null || google.fullName().isBlank()
                    ? google.email().substring(0, google.email().indexOf('@')) : google.fullName();
            user = User.builder()
                    .fullName(safeName)
                    .email(google.email())
                    .password(passwordEncoder.encode(UUID.randomUUID() + "-google-only"))
                    .authProvider(AuthProvider.GOOGLE)
                    .googleSubject(google.subject())
                    .avatarUrl(google.avatarUrl())
                    .role(Role.USER)
                    .enabled(true)
                    .build();
        } else {
            if (!user.isEnabled()) {
                throw new BadRequestException("This account is disabled");
            }
            if (user.getGoogleSubject() != null && !user.getGoogleSubject().equals(google.subject())) {
                throw new BadRequestException("This email is already linked to another Google account");
            }
            user.setGoogleSubject(google.subject());
            user.setAuthProvider(user.getAuthProvider() == AuthProvider.LOCAL ? AuthProvider.BOTH : AuthProvider.GOOGLE);
            if ((user.getAvatarUrl() == null || user.getAvatarUrl().isBlank()) && google.avatarUrl() != null) {
                user.setAvatarUrl(google.avatarUrl());
            }
        }
        return tokenResponse(userRepository.save(user), null);
    }

    private AuthResponse tokenResponse(User user, String recoveryCode) {
        UserDetails details = userDetailsService.loadUserByUsername(user.getEmail());
        return new AuthResponse(
                jwtService.generateToken(details),
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                user.getAvatarUrl(),
                user.getAuthProvider().name(),
                recoveryCode
        );
    }
}
