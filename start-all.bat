@echo off
echo ========================================================
echo  Launching LedgerLine Full-Stack Banking Application
echo ========================================================
start "LedgerLine Backend (Spring Boot)" cmd /k "cd /d "%~dp0Backend\backend\ledgerline" && mvn spring-boot:run"
timeout /t 5 /nobreak >nul
start "LedgerLine Frontend (React Vite)" cmd /k "cd /d "%~dp0frontend" && npm run dev"
echo.
echo Backend is starting at:  http://localhost:8080/api/health
echo Frontend will run at:    http://localhost:5173
echo.
pause

