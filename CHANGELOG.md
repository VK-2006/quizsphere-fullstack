# Changelog

## 2.0.0 — Email-free account recovery

- Replaced email OTP, Gmail SMTP, Resend, and Brevo integrations.
- Added security question and BCrypt-hashed answer during local registration.
- Added one-time recovery-code generation and display.
- Added question → answer → recovery-code → reset-token password recovery.
- Added recovery-code-protected security-question replacement.
- Added Profile-based recovery setup for existing and Google users.
- Added attempt limits and temporary recovery lockout.
- Added light/dark theme support and removed demo credentials.
- Updated Docker, Railway, Vercel, SQL, Postman, and documentation.

## 1.0.0 — Initial full-stack quiz platform

- React frontend, Spring Boot backend, and MySQL database.
- JWT authentication, Google Sign-In, profile, quizzes, attempts, result/review/history, and admin modules.
