# QuizSphere Full-Stack Quiz Application

QuizSphere is a complete interactive quiz platform built with React, Spring Boot, and MySQL. It supports user and admin workflows, JWT authentication, Google Sign-In, quiz attempts, results, review, history, profile management, dark mode, and email-free password recovery.

## Main features

### User

- Register and sign in
- Optional Google Sign-In
- Choose a security question during local registration
- Receive a one-time recovery code after registration
- Browse published quizzes by category and difficulty
- Take timed quizzes
- View score, percentage, pass/fail result, review, and attempt history
- Update profile information
- Configure or replace account-recovery settings
- Reset a forgotten password without email, SMTP, Resend, or Brevo

### Admin

- Admin dashboard
- Manage categories
- Create, update, publish, and delete quizzes
- Add, update, and delete questions and options
- View users and enable/disable accounts

## Technology stack

- Frontend: React 19, Vite, React Router, Axios, Bootstrap, Bootstrap Icons
- Backend: Java 21, Spring Boot, Spring Security, Spring Data JPA, Bean Validation
- Database: MySQL 8
- Authentication: JWT, BCrypt, Google Identity Services
- Deployment: Railway backend/MySQL and Vercel frontend

## Project structure

```text
quizsphere-fullstack/
├── backend/                 Spring Boot backend
├── frontend/                React/Vite frontend
├── database/                Full schema and migration
├── docs/                    API, deployment, and recovery guides
├── postman/                 Postman collection
├── scripts/                 Windows build/run scripts
├── docker-compose.yml
├── DEPLOYMENT_VERCEL_RAILWAY.md
├── START_HERE.md
└── README.md
```

## Local prerequisites

- Java 21 or later
- Maven 3.9+
- Node.js 22+
- npm
- MySQL 8+

## Database

Create the database manually using:

```sql
CREATE DATABASE quizsphere CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

The backend defaults to `spring.jpa.hibernate.ddl-auto=update`, so JPA creates or updates tables automatically. The complete SQL is also available at `database/schema.sql`.

## Backend environment variables

Copy `backend/.env.example` values into your terminal, IDE, Docker environment, or Railway Variables page.

Required production variables:

```env
DB_URL=jdbc:mysql://HOST:PORT/DATABASE?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=YOUR_DATABASE_USER
DB_PASSWORD=YOUR_DATABASE_PASSWORD
JPA_DDL_AUTO=update
JWT_SECRET=YOUR_PRIVATE_BASE64_SECRET
JWT_EXPIRATION_MS=86400000
FRONTEND_URL=https://YOUR_FRONTEND_DOMAIN
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

No SMTP, Gmail App Password, Resend, or Brevo variables are required. Sample admin/quiz seeding is disabled by default; enable it only after setting a private admin email and password.

## Frontend environment variables

Create `frontend/.env` from `frontend/.env.example`:

```env
VITE_API_URL=http://localhost:8080/api
VITE_GOOGLE_CLIENT_ID=YOUR_GOOGLE_WEB_CLIENT_ID.apps.googleusercontent.com
```

## Start locally

### Backend

```bat
cd backend
mvn clean spring-boot:run
```

### Frontend

```bat
cd frontend
npm ci --include=optional
npm run dev
```

Open `http://localhost:5173`.

## Account-recovery flow

### New local user

1. Register with name, email, password, security question, and security answer.
2. Save the generated recovery code such as `QSR-ABCD-EFGH-JKLM`.
3. For password recovery:
   - Enter email
   - Answer the security question
   - Enter the recovery code
   - Set a new password

### Existing user

Sign in and open `Profile → Account Recovery`. Enter the current password, choose a question and answer, and save the newly generated recovery code.

### Forgot security answer

The user may replace the security question only by providing the existing recovery code. A new code is generated and the old one becomes invalid.

## Security notes

- Passwords, security answers, and recovery codes are stored as BCrypt hashes.
- Challenge/reset tokens are random, short-lived, single-use, and stored as SHA-256 hashes.
- Recovery attempts are limited; repeated failures temporarily lock recovery.
- JWT secrets and API credentials must never be committed.
- The recovery code is displayed only when generated or regenerated.

## Build

```bat
scripts\build-windows.bat
```

The script builds backend and frontend without adding generated folders to Git.

## Deployment

Read `DEPLOYMENT_VERCEL_RAILWAY.md` for Railway and Vercel steps.
