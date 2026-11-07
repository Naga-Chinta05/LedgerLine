@echo off
echo Starting LedgerLine Spring Boot Backend on http://localhost:8080 ...
cd /d "%~dp0Backend\backend\ledgerline"
mvn spring-boot:run
pause

