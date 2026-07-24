# QuizSphere Complete Project — Start Here

This ZIP contains the **entire QuizSphere project**, not a patch.

Included:

- React + Vite frontend
- Spring Boot backend
- MySQL schema and account-recovery migration
- JWT authentication and role-based authorization
- Google Sign-In support
- User/admin quiz functionality
- Light/dark mode
- Email-free password recovery using a security question plus recovery code
- Docker, Railway, and Vercel configuration
- Postman collection and documentation

## Replace your current project

1. Back up your current folder:
   `C:\Projects\quizsphere-fullstack`
2. Extract this ZIP.
3. Copy the extracted `quizsphere-fullstack` folder to:
   `C:\Projects\quizsphere-fullstack`
4. Do not copy `node_modules`, `target`, or old `.env` files from the previous project.

## Build on Windows

Run:

```bat
cd /d "C:\Projects\quizsphere-fullstack"
scripts\build-windows.bat
```

## Important for existing users

Accounts created before this recovery feature do not have a security question or recovery code. Those users must sign in and open:

`Profile → Account Recovery → Configure`

A new recovery code is displayed once. Save it securely.
