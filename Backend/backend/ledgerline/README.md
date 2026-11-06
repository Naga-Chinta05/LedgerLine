# LedgerLine Backend

Java 21 + Spring Boot + PostgreSQL backend foundation for the LedgerLine money movement challenge.

## Included
- Maven/Spring Boot project structure
- PostgreSQL connection configuration
- Flyway initial schema migration
- JPA entities for users, accounts, transactions, ledger entries, idempotency records and audit logs
- Initial REST health endpoint
- Initial Spring Security configuration and BCrypt password encoder
- CORS configuration for the Vite frontend

## Prerequisites
- JDK 21
- PostgreSQL
- Windows CMD or PowerShell
- Internet access for Maven dependency downloads

## 1. Create the PostgreSQL database

Connect as a PostgreSQL administrator:

```sql
CREATE USER ledgerline_app WITH PASSWORD 'choose_a_local_password';
CREATE DATABASE ledgerline_db OWNER ledgerline_app;
```

If the user or database already exists, do not create it again.

## 2. Configure the database password

Windows CMD:

```cmd
set "DB_PASSWORD=choose_a_local_password"
```

PowerShell:

```powershell
$env:DB_PASSWORD = "choose_a_local_password"
```

The password must match the PostgreSQL user's password.

## 3. Run the backend

From this folder (the folder containing pom.xml):

```cmd
mvn spring-boot:run
```

If you generate a Maven Wrapper, you can instead use:

```cmd
mvnw.cmd spring-boot:run
```

## 4. Check health

Open:

http://localhost:8080/api/health

Expected response:

```json
{
  "application": "LedgerLine",
  "status": "UP",
  "message": "LedgerLine backend is running"
}
```

## Important implementation status

This ZIP is a backend foundation, not a completed banking system. It includes the project structure, schema, entities, health endpoint and initial security configuration. JWT login, registration, account APIs, deposit/withdrawal/transfer services, production-grade idempotency, ledger posting, concurrency workflows and full audit service still need implementation and testing.

The current security configuration uses HTTP Basic as a temporary development authentication mechanism. Do not expose this application publicly or use it for real money. Replace it with the intended authentication design and complete security review before deployment.

## Database integrity notes

- Monetary values use PostgreSQL NUMERIC(19,2) and Java BigDecimal.
- Account balances have a database non-negative check.
- Idempotency keys are unique per user.
- Ledger entries are represented separately from account balances.
- Application services must ensure every posted transaction has balanced debit and credit entries in one database transaction.
