# Google Sign-In and Profile Setup

## Google Cloud

1. Create or select a Google Cloud project.
2. Configure the OAuth consent screen.
3. Create an OAuth 2.0 Web Client ID.
4. Add local and deployed frontend origins.

Example authorized JavaScript origins:

```text
http://localhost:5173
https://quizsphere-fullstack.vercel.app
```

## Environment variables

Backend:

```env
GOOGLE_CLIENT_ID=YOUR_GOOGLE_WEB_CLIENT_ID.apps.googleusercontent.com
```

Frontend:

```env
VITE_GOOGLE_CLIENT_ID=YOUR_GOOGLE_WEB_CLIENT_ID.apps.googleusercontent.com
```

Both values must use the same Web Client ID.

## Google-only accounts

A Google-only user may open `Profile → Account Recovery` and configure a security question and recovery code without entering a current local password. After a password reset, the account supports both Google and password sign-in.
