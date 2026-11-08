@echo off
setlocal
set "ROOT=%~dp0"
echo Starting LedgerLine Spring Boot Backend...
cd /d "%ROOT%Backend\backend\ledgerline"
mvn spring-boot:run -Dspring-boot.run.profiles=dev
pause
