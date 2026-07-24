@echo off
setlocal EnableExtensions
set "ROOT=%~dp0.."

if not exist "%ROOT%\backend\pom.xml" (
  echo [ERROR] backend\pom.xml not found.
  exit /b 1
)

if defined JAVA_HOME (
  echo Using JAVA_HOME=%JAVA_HOME%
) else (
  echo [INFO] JAVA_HOME is not set. Maven will use the Java available in PATH.
)

java -version || exit /b 1
mvn -version || exit /b 1
node -v || exit /b 1
npm -v || exit /b 1

echo.
echo [1/2] Building backend...
pushd "%ROOT%\backend"
call mvn clean package -DskipTests
if errorlevel 1 exit /b 1
popd

echo.
echo [2/2] Building frontend...
pushd "%ROOT%\frontend"
call npm ci --include=optional
if errorlevel 1 exit /b 1
call npm run build
if errorlevel 1 exit /b 1
popd

echo.
echo [SUCCESS] Backend and frontend builds completed.
