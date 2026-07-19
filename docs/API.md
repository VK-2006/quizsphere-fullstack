# API behavior notes

1. Register or log in to obtain a JWT.
2. Send the token as `Authorization: Bearer <token>`.
3. Starting a quiz creates an `IN_PROGRESS` attempt and returns questions without correctness flags.
4. Submitting sends question IDs and selected option IDs.
5. The server validates option ownership, calculates the score, stores answer records, and returns the result.
6. Review is available only after submission.

## Password reset flow

1. `POST /api/auth/forgot-password` with the registered email address.
2. The backend creates a six-digit OTP, stores only its BCrypt hash, and sends the raw OTP by email.
3. `POST /api/auth/verify-reset-otp` with the email and OTP.
4. Successful verification returns a short-lived one-time reset token.
5. `POST /api/auth/reset-password` with the reset token, new password, and confirmation.
6. The backend stores the new password as a BCrypt hash and invalidates the reset record.

The request-OTP endpoint returns a generic response even when an email address is not registered. This reduces account-enumeration risk.


## Google authentication

- `POST /api/auth/google` — verify a Google Identity Services ID token and sign up/sign in the user.

Request:

```json
{ "credential": "GOOGLE_ID_TOKEN" }
```

## User profile

- `GET /api/profile` — retrieve the authenticated user profile and quiz statistics.
- `PUT /api/profile` — update name, bio, phone, location, date of birth, and avatar URL.
