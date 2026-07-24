# QuizSphere REST API

Base URL: `/api`

Authenticated endpoints require:

```http
Authorization: Bearer <JWT_TOKEN>
```

## Health

- `GET /health`

## Authentication

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/google`
- `GET /auth/me`

### Register body

```json
{
  "fullName": "Venkat Kiran",
  "email": "user@example.com",
  "password": "SecurePassword123",
  "securityQuestion": "What private recovery phrase did you choose?",
  "securityAnswer": "my private phrase"
}
```

The registration response includes `recoveryCode` once. The client must show it and should not save it in local storage.

## Email-free password recovery

1. `POST /auth/recovery-question`
2. `POST /auth/verify-security-answer`
3. `POST /auth/verify-recovery-code`
4. `POST /auth/reset-password`

### Recovery question

```json
{"email":"user@example.com"}
```

### Verify security answer

```json
{
  "email":"user@example.com",
  "securityAnswer":"my private phrase"
}
```

### Verify recovery code

```json
{
  "email":"user@example.com",
  "challengeToken":"TOKEN_FROM_PREVIOUS_RESPONSE",
  "recoveryCode":"QSR-ABCD-EFGH-JKLM"
}
```

### Reset password

```json
{
  "resetToken":"TOKEN_FROM_PREVIOUS_RESPONSE",
  "newPassword":"NewSecurePassword123",
  "confirmPassword":"NewSecurePassword123"
}
```

### Replace forgotten security question

- `POST /auth/reset-security-question`

This endpoint requires the existing recovery code and returns a new recovery code plus a reset token.

## Profile

- `GET /profile`
- `PUT /profile`
- `GET /profile/recovery`
- `PUT /profile/recovery`

`PUT /profile/recovery` regenerates the recovery code and invalidates the previous code.

## Public quiz data

- `GET /categories`
- `GET /quizzes`
- `GET /quizzes/{id}`

## Authenticated quiz workflow

- `POST /quizzes/{id}/start`
- `POST /attempts/{id}/submit`
- `GET /attempts/{id}/result`
- `GET /attempts/{id}/review`
- `GET /users/me/attempts`

## Admin

Base: `/admin`

- `GET /dashboard`
- `POST /categories`
- `PUT /categories/{id}`
- `DELETE /categories/{id}`
- `GET /quizzes`
- `POST /quizzes`
- `PUT /quizzes/{id}`
- `DELETE /quizzes/{id}`
- `GET /quizzes/{quizId}/questions`
- `POST /quizzes/{quizId}/questions`
- `PUT /questions/{id}`
- `DELETE /questions/{id}`
- `GET /users`
- `PATCH /users/{id}/toggle`
