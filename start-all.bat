@echo off
setlocal
set "ROOT=%~dp0"
echo ========================================================
echo  Launching LedgerLine Full-Stack Banking Application
echo ========================================================

start "LedgerLine Backend" cmd /k "cd /d "%ROOT%Backend\backend\ledgerline" && mvn spring-boot:run -Dspring-boot.run.profiles=dev"
timeout /t 5 /nobreak >nul
start "LedgerLine Frontend" cmd /k "cd /d "%ROOT%frontend" && npm run dev"

echo.
echo ========================================================
echo  Backend:   http://localhost:8080/api/health
echo  Frontend:  http://localhost:5173
echo ========================================================
