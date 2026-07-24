@echo off
setlocal
cd /d "%~dp0..\frontend"
if errorlevel 1 exit /b 1

echo [1/2] Installing frontend dependencies...
call npm ci --include=optional
if errorlevel 1 exit /b 1

echo [2/2] Building responsive QuizSphere UI...
call npm run build
if errorlevel 1 exit /b 1

echo.
echo [SUCCESS] Responsive UI, animations, and dark mode compiled successfully.
endlocal
