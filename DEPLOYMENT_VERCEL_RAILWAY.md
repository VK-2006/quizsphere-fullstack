# QuizSphere Deployment — Railway + Vercel

## Railway backend variables

Add these to the backend service:

```env
DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=${{MySQL.MYSQLUSER}}
DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
JPA_DDL_AUTO=update
JWT_SECRET=YOUR_PRIVATE_BASE64_SECRET
JWT_EXPIRATION_MS=86400000
FRONTEND_URL=https://quizsphere-fullstack.vercel.app
GOOGLE_CLIENT_ID=YOUR_GOOGLE_WEB_CLIENT_ID.apps.googleusercontent.com
RECOVERY_CHALLENGE_EXPIRY_MINUTES=10
RESET_TOKEN_EXPIRY_MINUTES=10
RECOVERY_MAX_ATTEMPTS=5
RECOVERY_LOCK_MINUTES=15
SEED_DATA_ENABLED=false
ADMIN_EMAIL=
ADMIN_PASSWORD=
ADMIN_FULL_NAME=QuizSphere Admin
```

Delete all obsolete email variables:

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

`RESET_TOKEN_EXPIRY_MINUTES` must remain.

## Railway settings

- Root directory: `backend`
- Build: Dockerfile or `mvn clean package -DskipTests`
- Start: handled by the Spring Boot JAR/Dockerfile
- Health check: `/api/health`

Expected response:

```json
{"status":"UP"}
```

## Vercel frontend variables

Set for Production, Preview, and Development as needed:

```env
VITE_API_URL=https://quizsphere-fullstack-production.up.railway.app/api
VITE_GOOGLE_CLIENT_ID=YOUR_GOOGLE_WEB_CLIENT_ID.apps.googleusercontent.com
```

Vercel project settings:

- Root directory: `frontend`
- Framework preset: Vite
- Build command: `npm run build`
- Output directory: `dist`

Environment-variable changes require a new deployment.

## CORS

`FRONTEND_URL` in Railway must exactly match the browser origin, without a trailing slash:

```env
FRONTEND_URL=https://quizsphere-fullstack.vercel.app
```

## Existing accounts

After deployment, old users must sign in and configure recovery from Profile before Forgot Password can be used.

## Optional admin/sample data

Set `SEED_DATA_ENABLED=true` only when `ADMIN_EMAIL` and a private `ADMIN_PASSWORD` of at least 8 characters are configured. After the initial seed, set it back to `false`. No admin password is hard-coded in the source.
