# Password Reset Email Setup

QuizSphere sends password-reset OTPs through Spring Boot's mail integration. The default SMTP settings target Gmail on port 587 with STARTTLS.

## Gmail sender setup

1. Choose the Gmail account that will send QuizSphere OTP messages.
2. Enable 2-Step Verification on that Google Account.
3. Create an App Password for QuizSphere.
4. Copy the 16-character App Password. When setting the environment variable, remove display spaces if Google shows any.
5. Never put the normal Gmail password or the App Password in Git, source code, screenshots, or README files.

## Windows Command Prompt

Run these commands in the same Command Prompt window used to start Spring Boot:

```bat
cd /d "C:\path\to\quizsphere-fullstack\backend"
set "DB_USERNAME=root"
set "DB_PASSWORD=YOUR_MYSQL_PASSWORD"
set "MAIL_USERNAME=your.sender@gmail.com"
set "MAIL_APP_PASSWORD=YOUR_16_CHARACTER_APP_PASSWORD"
set "MAIL_FROM=your.sender@gmail.com"
mvn spring-boot:run
```

## PowerShell

```powershell
cd "C:\path\to\quizsphere-fullstack\backend"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="YOUR_MYSQL_PASSWORD"
$env:MAIL_USERNAME="your.sender@gmail.com"
$env:MAIL_APP_PASSWORD="YOUR_16_CHARACTER_APP_PASSWORD"
$env:MAIL_FROM="your.sender@gmail.com"
mvn spring-boot:run
```

## Generic SMTP provider

These environment variables are supported:

```text
MAIL_HOST
MAIL_PORT
MAIL_USERNAME
MAIL_APP_PASSWORD
MAIL_FROM
MAIL_SMTP_AUTH
MAIL_STARTTLS_ENABLE
MAIL_STARTTLS_REQUIRED
```

Change the host, port, and TLS values to match the selected provider.

## Reset security defaults

```text
OTP expiry: 10 minutes
Maximum incorrect OTP attempts: 5
Resend cooldown: 60 seconds
Reset-token expiry after OTP verification: 10 minutes
```

Optional overrides:

```text
OTP_EXPIRY_MINUTES
OTP_MAX_ATTEMPTS
OTP_RESEND_COOLDOWN_SECONDS
RESET_TOKEN_EXPIRY_MINUTES
```
