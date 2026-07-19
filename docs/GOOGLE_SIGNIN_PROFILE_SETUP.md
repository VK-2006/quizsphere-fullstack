# Google Sign-In and User Profile Setup

## 1\. Create Google OAuth credentials

1. Open Google Cloud Console.
2. Create or select a project.
3. Configure **Google Auth Platform / OAuth consent screen**.
4. Create an **OAuth client ID** with application type **Web application**.
5. Add this Authorized JavaScript origin for local development:

   * `http://localhost:5173`
6. Copy the generated Web Client ID. The same client ID must be used by React and Spring Boot.

No Google client secret is required for this ID-token flow. The frontend receives an ID token and the backend verifies its signature, issuer, expiry, and audience.

## 2\. Backend environment variables (CMD)

```bat
set "DB\_USERNAME=root"

set "DB\_PASSWORD=YOUR\_DB\_PASSWORD"

set "MAIL\_USERNAME=YOUR\_GMAIL@gmail.com"
set "MAIL\_APP\_PASSWORD=YOUR\_NEW\_APP\_PASSWORD\_WITHOUT\_SPACES"
set "MAIL\_FROM=YOUR\_GMAIL@gmail.com"
set "GOOGLE\_CLIENT\_ID=YOUR\_WEB\_CLIENT\_ID.apps.googleusercontent.com"
mvn spring-boot:run
```

## 3\. Frontend `.env`

Create `frontend/.env`:

```env
VITE\_API\_URL=http://localhost:8080/api
VITE\_GOOGLE\_CLIENT\_ID=YOUR\_WEB\_CLIENT\_ID.apps.googleusercontent.com
```

Then run:

```bat
npm install
npm run dev
```

## 4\. Database update

Spring JPA `ddl-auto=update` automatically adds profile and Google-account columns to the existing `users` table. Existing email/password accounts continue to work. When the same verified email signs in with Google, the account is securely linked and the provider becomes `BOTH`.

## 5\. Profile fields

Users can edit full name, bio, phone, location, date of birth, and avatar URL. The profile also displays attempts, passed quizzes, average score, provider, and joining date.

