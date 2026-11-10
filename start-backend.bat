@echo off
setlocal
cd /d "%~dp0Backend\backend\ledgerline"
echo ========================================================
echo   Starting LedgerLine Spring Boot Backend (Port 8085)
echo ========================================================
mvn spring-boot:run
pause
