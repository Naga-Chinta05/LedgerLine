@echo off
setlocal
cd /d "%~dp0frontend"
echo ========================================================
echo   Starting LedgerLine React Vite Frontend (Port 5173)
echo ========================================================
npm run dev
pause
