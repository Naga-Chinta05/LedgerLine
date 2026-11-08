@echo off
setlocal
set "ROOT=%~dp0"
echo Starting LedgerLine React Frontend...
cd /d "%ROOT%frontend"
npm run dev
pause
