@echo off
setlocal
cd /d "%~dp0"
echo ========================================================
echo  Launching LedgerLine Full-Stack Banking Application
echo ========================================================

start "LedgerLine Backend" "%~dp0start-backend.bat"
timeout /t 4 /nobreak >nul
start "LedgerLine Frontend" "%~dp0start-frontend.bat"

echo.
echo ========================================================
echo  Backend:   http://localhost:8085/api/health
echo  Frontend:  http://localhost:5173
echo ========================================================
