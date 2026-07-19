# Changelog

## 1.1.0 — Email OTP password recovery

- Added **Forgot password?** link to the sign-in page.
- Added email entry, six-digit OTP verification, resend timer, and new-password UI.
- Added Spring Boot mail integration with generic SMTP environment variables.
- Added secure OTP persistence using BCrypt hashes.
- Added 10-minute OTP expiry, five-attempt limit, and 60-second resend cooldown.
- Added one-time reset-token verification before changing the password.
- Added MySQL password reset table and an upgrade SQL script.
- Added Postman requests and a password reset integration test.
