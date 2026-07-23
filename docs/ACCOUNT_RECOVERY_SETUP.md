# QuizSphere Email-Free Account Recovery

QuizSphere now resets passwords without SMTP, Gmail, Resend, or Brevo.

## Registration flow

1. User enters name, email, password, security question, and security answer.
2. The security answer is normalized and stored only as a BCrypt hash.
3. A random recovery code such as `QSR-ABCD-EFGH-JKLM` is generated.
4. The recovery code is displayed once. Only its BCrypt hash is stored.

## Forgot-password flow

1. Enter registered email.
2. Answer the stored security question.
3. Enter the recovery code.
4. A short-lived, single-use reset token is issued.
5. Set a new password.

## Forgotten security answer

The question cannot be replaced after a wrong answer alone. The existing recovery code is required. After successful replacement:

- the security question and answer are updated;
- the old recovery code is invalidated;
- a new recovery code is generated and displayed once;
- the user can continue to set a new password.

## Existing accounts

Existing users do not yet have recovery settings. They must sign in and open:

`Profile -> Account recovery`

Local/BOTH accounts must enter the current password before creating or replacing recovery settings. Google-only accounts use their authenticated Google session.

## Railway environment variables

```env
RECOVERY_CHALLENGE_EXPIRY_MINUTES=10
RESET_TOKEN_EXPIRY_MINUTES=10
RECOVERY_MAX_ATTEMPTS=5
RECOVERY_LOCK_MINUTES=15
```

These old email variables are no longer used and can be removed after the new deployment is healthy:

```text
BREVO_API_KEY
BREVO_FROM_EMAIL
BREVO_FROM_NAME
RESEND_API_KEY
RESEND_FROM
RESEND_REPLY_TO
MAIL_HOST
MAIL_PORT
MAIL_USERNAME
MAIL_APP_PASSWORD
MAIL_FROM
MAIL_SMTP_AUTH
MAIL_STARTTLS_ENABLE
MAIL_STARTTLS_REQUIRED
SPRING_MAIL_TEST_CONNECTION
OTP_EXPIRY_MINUTES
OTP_RESEND_COOLDOWN_SECONDS
OTP_MAX_ATTEMPTS
```

Keep database, JWT, frontend URL, Google client ID, and `RESET_TOKEN_EXPIRY_MINUTES` variables.

## Database

With `JPA_DDL_AUTO=update`, Hibernate adds the new user columns and creates `password_recovery_sessions` automatically. The old `password_reset_otps` table may remain unused and can be removed later after a verified backup.
