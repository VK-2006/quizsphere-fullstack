# QuizSphere Production Deployment

## Deployment architecture

- GitHub: full monorepo source code
- Vercel: `frontend/` React + Vite application
- Railway: `backend/` Spring Boot API
- Railway MySQL: production database

## Railway backend variables

Create a Railway project, add a MySQL service, and deploy the same GitHub repository as a backend service with Root Directory `/backend`.

Set these variables on the backend service. Replace placeholders and use Railway's reference-variable autocomplete for the MySQL values.

```env
DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=${{MySQL.MYSQLUSER}}
DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
JPA_DDL_AUTO=update
JWT_SECRET=REPLACE_WITH_A_NEW_BASE64_SECRET
JWT_EXPIRATION_MS=86400000
FRONTEND_URL=http://localhost:5173
GOOGLE_CLIENT_ID=REPLACE_WITH_GOOGLE_WEB_CLIENT_ID
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=REPLACE_WITH_SENDER_GMAIL
MAIL_APP_PASSWORD=REPLACE_WITH_NEW_GMAIL_APP_PASSWORD
MAIL_FROM=REPLACE_WITH_SENDER_GMAIL
MAIL_SMTP_AUTH=true
MAIL_STARTTLS_ENABLE=true
MAIL_STARTTLS_REQUIRED=true
```

After the backend deployment succeeds, generate a Railway public domain and test:

```text
https://YOUR-BACKEND.up.railway.app/api/health
```

## Vercel frontend project

Import the same GitHub repository into Vercel and set Root Directory to `frontend`.

Framework preset: Vite

Build command:

```text
npm run build
```

Output directory:

```text
dist
```

Add these environment variables to Production, Preview, and Development as needed:

```env
VITE_API_URL=https://YOUR-BACKEND.up.railway.app/api
VITE_GOOGLE_CLIENT_ID=REPLACE_WITH_GOOGLE_WEB_CLIENT_ID
```

Deploy and copy the final Vercel production URL.

## Final connection steps

1. In Railway backend variables, replace `FRONTEND_URL` with the exact Vercel production origin, such as `https://quizsphere.vercel.app`.
2. Redeploy the Railway backend.
3. In Google Auth Platform, add the exact Vercel production origin under Authorized JavaScript origins.
4. Keep `http://localhost:5173` as an additional Authorized JavaScript origin for local development.
5. Do not add paths such as `/login`, and do not add a trailing slash.
6. Redeploy Vercel after changing Vercel environment variables.

## Security

Never commit `.env` files, database credentials, Gmail App Passwords, OAuth client secrets, or JWT secrets. The Google Web Client ID is used in the frontend and backend; the Google Client Secret is not required by this ID-token flow.
