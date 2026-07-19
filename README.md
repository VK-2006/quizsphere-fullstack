# QuizSphere

**Full Stack Interactive Quiz Application using React, Spring Boot, and MySQL**

> Add your full name here before submitting the repository.

QuizSphere is a complete full-stack quiz platform with role-based login, a timed quiz experience, secure backend scoring, answer review, attempt history, and an admin content studio.

## Included features

### Learner
- Register and sign in
- Forgot-password recovery using a 6-digit email OTP
- OTP verification followed by secure password reset
- Browse published quizzes
- Start a timed quiz
- Select and change answers
- Automatic submission when the timer ends
- Backend score and pass/fail calculation
- Result page and answer-by-answer review
- Personal attempt history and dashboard

### Administrator
- Admin dashboard metrics
- Category create, edit, and delete
- Quiz create, edit, publish, and delete
- Question and option management
- Exactly-one-correct-option validation
- User listing and enable/disable control

## Technology stack

- Frontend: React, Vite, React Router, Axios, Bootstrap
- Backend: Java 21, Spring Boot, Spring Security, Spring Data JPA
- Authentication: JWT, BCrypt, and email OTP password recovery
- Database: MySQL 8.4 LTS
- Build tools: npm and Maven
- Optional local infrastructure: Docker Compose

## Repository structure

```text
quizsphere-fullstack/
├── frontend/           React application
├── backend/            Spring Boot REST API
├── database/           MySQL bootstrap script
├── postman/            API collection
├── docker-compose.yml  MySQL + backend containers
└── README.md
```

## Prerequisites

Install:

- Java JDK 21
- Maven 3.9+
- Node.js supported by the included Vite version
- MySQL 8.4, or Docker Desktop

Verify Java is not mismatched:

```bash
java -version
javac -version
mvn -version
node -v
npm -v
```

Java 21 is recommended. The Maven build also explicitly enables Lombok annotation processing, so the project can compile when Maven itself is launched with JDK 23, 24, 25, or 26.

## Option A: Run MySQL with Docker

From the repository root:

```bash
docker compose up -d mysql
```

MySQL will be available at `localhost:3306` with:

```text
Database: quizsphere
Username: root
Password: root
```

## Option B: Use installed MySQL

Run:

```sql
CREATE DATABASE quizsphere CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Then change the backend environment variables or edit `backend/src/main/resources/application.properties`.

## Start the backend

Windows PowerShell:

```powershell
cd backend
$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"
mvn spring-boot:run
```

Windows Command Prompt:

```bat
cd backend
set DB_USERNAME=root
set DB_PASSWORD=root
mvn spring-boot:run
```

Linux/macOS:

```bash
cd backend
DB_USERNAME=root DB_PASSWORD=root mvn spring-boot:run
```

Backend URL: `http://localhost:8080`

Health endpoint: `http://localhost:8080/api/health`

On first start, the application creates a demo quiz and an admin account:

```text
Email: admin@quizsphere.com
Password: Admin@123
```

Change the password and JWT secret before production deployment.

## Configure Gmail OTP email

The forgot-password feature sends a 6-digit OTP using Spring Boot mail support. For Gmail, enable 2-Step Verification on the sender Google Account and create a Google **App Password**. Do not use the normal Gmail password.

Windows Command Prompt (run these in the same terminal before starting the backend):

```bat
set DB_USERNAME=root
set DB_PASSWORD=YOUR_MYSQL_PASSWORD
set MAIL_USERNAME=yourgmail@gmail.com
set MAIL_APP_PASSWORD=your_16_character_google_app_password
set MAIL_FROM=yourgmail@gmail.com
mvn spring-boot:run
```

PowerShell:

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="YOUR_MYSQL_PASSWORD"
$env:MAIL_USERNAME="yourgmail@gmail.com"
$env:MAIL_APP_PASSWORD="your_16_character_google_app_password"
$env:MAIL_FROM="yourgmail@gmail.com"
mvn spring-boot:run
```

The OTP expires after 10 minutes, allows at most 5 incorrect attempts, and can only be re-requested after 60 seconds. These values can be changed with `OTP_EXPIRY_MINUTES`, `OTP_MAX_ATTEMPTS`, and `OTP_RESEND_COOLDOWN_SECONDS`.


## Lombok/JDK 23+ troubleshooting

If Maven reports many errors such as `cannot find symbol: method builder()`, `getId()`, `getEmail()`, or setters on entity classes, Lombok annotation processing is not active. This corrected project explicitly registers Lombok `1.18.46` in `maven-compiler-plugin`, because recent JDK versions do not automatically discover annotation processors.

From the backend directory, clear the old failed compilation and rebuild:

```bat
mvn clean
mvn -U spring-boot:run
```

Confirm that Maven is using the expected Java installation:

```bat
mvn -version
java -version
javac -version
```

Do not run Maven from the repository root. The prompt must end with `quizsphere-fullstack\backend>` and `dir pom.xml` must display the backend POM.

## Start the frontend

Open a second terminal:

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

On Windows Command Prompt, replace the copy command with:

```bat
copy .env.example .env
```

Frontend URL: `http://localhost:5173`

## Production build

Frontend:

```bash
cd frontend
npm run build
```

Backend:

```bash
cd backend
mvn clean test
mvn clean package
java -jar target/quizsphere-backend-1.0.0.jar
```

## Main REST API endpoints

### Authentication

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/login` | Public |
| POST | `/api/auth/forgot-password` | Public |
| POST | `/api/auth/verify-reset-otp` | Public |
| POST | `/api/auth/reset-password` | Public |
| GET | `/api/auth/me` | Authenticated |

### Quiz taker

| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/categories` | Public |
| GET | `/api/quizzes` | Public |
| GET | `/api/quizzes/{id}` | Public |
| POST | `/api/quizzes/{id}/start` | User/Admin |
| POST | `/api/attempts/{id}/submit` | Attempt owner |
| GET | `/api/attempts/{id}/result` | Attempt owner |
| GET | `/api/attempts/{id}/review` | Attempt owner |
| GET | `/api/users/me/attempts` | Authenticated |

### Administrator

| Method | Endpoint |
|---|---|
| GET | `/api/admin/dashboard` |
| POST/PUT/DELETE | `/api/admin/categories` |
| GET/POST/PUT/DELETE | `/api/admin/quizzes` |
| GET/POST | `/api/admin/quizzes/{quizId}/questions` |
| PUT/DELETE | `/api/admin/questions/{id}` |
| GET | `/api/admin/users` |
| PATCH | `/api/admin/users/{id}/toggle` |

## Security notes

- Passwords and OTPs are stored as BCrypt hashes; raw OTP values are never saved.
- OTPs expire, have a retry limit, and verification issues a one-time short-lived reset token.
- Correct-answer flags are not returned in the quiz-taking payload.
- The Spring Boot backend calculates all marks and percentages.
- Admin APIs require the `ADMIN` role.
- JWT secrets and database credentials support environment variables.
- The included secret and admin password are demonstration values only.

## Suggested GitHub submission steps

```bash
git init
git add .
git commit -m "Build QuizSphere full-stack quiz application"
git branch -M main
git remote add origin YOUR_REPOSITORY_URL
git push -u origin main
```

Set the repository description to something like:

```text
QuizSphere – Full-stack interactive quiz application by YOUR NAME using React, Spring Boot, and MySQL.
```

## Known MVP boundaries

This version supports single-answer multiple-choice questions. Features such as refresh tokens, leaderboards, image questions, transactional email providers, and production deployment pipelines can be added as future enhancements.


## Google Sign-In and User Profiles

The upgraded project supports email/password plus Google Sign-In, automatic account linking by verified email, Google avatars, and editable learner profiles. See `docs/GOOGLE_SIGNIN_PROFILE_SETUP.md` for Google Cloud and environment setup.
