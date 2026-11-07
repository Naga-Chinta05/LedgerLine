# LedgerLine – Secure Digital Banking & Transaction Ledger System
**Snovasys Fresher Hiring Challenge 2026**

LedgerLine is a secure, production-grade digital banking and transaction ledger platform built with **Java 21, Spring Boot 3, PostgreSQL, Spring Security (JWT), and React.js (Vite)**. It enforces double-entry bookkeeping, deadlock-free concurrency control, idempotency to eliminate duplicate charges, strict balance non-negativity, and automated zero-sum ledger reconciliation.

---

## 🏛️ System Architecture & Financial Principles

### 1. Balanced Double-Entry Bookkeeping
Every transaction records paired **DEBIT** and **CREDIT** entries in a single database transaction boundary:
- **Deposit**: Debits `CENTRAL_CASH_RESERVE` and credits Customer Account.
- **Withdrawal**: Debits Customer Account and credits `ATM_CASH_DISBURSEMENT`.
- **Transfer**: Debits Source Customer Account and credits Destination Customer Account.
- **Zero-Sum Invariant**: $\sum \text{Debits} = \sum \text{Credits}$ across the entire ledger at all times.

### 2. Concurrency & Deadlock Prevention
- Critical balance operations use JPA pessimistic write locking (`@Lock(LockModeType.PESSIMISTIC_WRITE)`).
- **Global Ascending Lock Ordering**: When transferring between two accounts, locks are always acquired in order of `Math.min(acc1.id, acc2.id)` followed by `Math.max(acc1.id, acc2.id)`. This mathematically eliminates cyclic wait conditions (Coffman deadlock condition) during concurrent bidirectional transfers.

### 3. Idempotency Engine
- Mutation endpoints accept an `Idempotency-Key` HTTP header.
- A deterministic cryptographic SHA-256 hash of the request payload is stored in `idempotency_records`.
- Replaying the same key with identical payload immediately returns the cached transaction without re-executing.
- Replaying the same key with modified parameters raises a `409 Conflict`.

### 4. Non-Negative Balance Enforcement
- Checked in domain service logic prior to debiting.
- Enforced at the database engine level via PostgreSQL `CHECK (balance >= 0)`.

### 5. Role-Based Access Control (RBAC)
- `ADMIN`: Account lifecycle administration (Freeze, Activate, Close), system-wide transaction inspection.
- `ACCOUNT_HOLDER`: Personal banking dashboard, multi-account management, instant money transfers, deposits, withdrawals.
- `AUDITOR`: Read-only compliance portal, zero-sum ledger equilibrium verifier, chronological audit trail inspection.

---

## 🛠️ Technology Stack

| Layer | Technologies |
|---|---|
| **Backend** | Java 21, Spring Boot 3.5+, Spring Security, Spring Data JPA / Hibernate, JJWT 0.12.6, Maven |
| **Database** | PostgreSQL, Flyway Schema Migrations, H2 Database (for isolated test suites) |
| **Frontend** | React 18, Vite, React Router DOM, Axios, Lucide Icons, Custom Fintech CSS |
| **Testing** | JUnit 5, Mockito, MockMvc, Spring Boot Test |

---

## 🚀 Pre-seeded Evaluation Accounts

The system includes an automated seeder (`DataInitializer`) that pre-populates demo accounts on first launch:

| Role | Email Address | Password | Initial Account | Starting Balance |
|---|---|---|---|---|
| **Administrator** | `admin@ledgerline.com` | `Admin@123` | System Admin | N/A |
| **Compliance Auditor** | `auditor@ledgerline.com` | `Auditor@123` | Audit Portal | N/A |
| **Account Holder (1)** | `john.doe@example.com` | `User@123` | `LL-1001001001` | ₹45,000.00 |
| **Account Holder (2)** | `jane.smith@example.com` | `User@123` | `LL-2002002002` | ₹30,000.00 |

> 💡 **Quick Login**: The frontend Login page provides **1-Click Demo Buttons** to instantly sign in as any of the roles above.

---

## 💻 Quick Start & Running Locally

### Option A: One-Click Launch (Windows)
Double-click `start-all.bat` in the repository root. This will launch:
1. Spring Boot backend on `http://localhost:8080`
2. React Vite frontend on `http://localhost:5173`

---

### Option B: Manual Terminal Execution

#### 1. Start the PostgreSQL Database
Connect to PostgreSQL and create the database and user (if not already existing):
```sql
CREATE USER ledgerline_app WITH PASSWORD 'postgres';
CREATE DATABASE ledgerline_db OWNER ledgerline_app;
```

#### 2. Start the Backend
From the repository root:
```bash
cd Backend/backend/ledgerline
mvn spring-boot:run
```
*Health Check:* [http://localhost:8080/api/health](http://localhost:8080/api/health)

#### 3. Start the Frontend
In a new terminal window:
```bash
cd frontend
npm install
npm run dev
```
Open your browser at [http://localhost:5173](http://localhost:5173).

---

## 🧪 Running Automated Tests

Run the backend unit and integration test suite:
```bash
cd Backend/backend/ledgerline
mvn test
```
The test suite validates:
- Deposit, withdrawal, and transfer workflows
- Non-negative balance constraint and overdraft rejection
- Double-entry ledger debit/credit zero-sum reconciliation
- Ascending-ID lock ordering for deadlock prevention
- Idempotency replay and conflict detection
- JWT authentication and registration endpoints

---

## 📡 Key REST APIs Reference

### Authentication (`/api/auth`)
- `POST /api/auth/register` - Create new user & auto-provision bank account
- `POST /api/auth/login` - Authenticate & obtain JWT Bearer token
- `GET /api/auth/me` - Get profile of authenticated user

### Banking Accounts (`/api/accounts`)
- `GET /api/accounts` - List all accounts belonging to authenticated user
- `POST /api/accounts` - Open an additional checking account
- `GET /api/accounts/{id}` - Get details of specific account
- `GET /api/accounts/number/{accountNumber}` - Lookup account by number (for transfers)
- `PATCH /api/accounts/{id}/status` - (Admin only) Update status (`ACTIVE`, `FROZEN`, `CLOSED`)

### Transactions & Money Movement (`/api/transactions`)
- `POST /api/transactions/deposit` - Deposit funds (Headers: `Idempotency-Key`)
- `POST /api/transactions/withdraw` - Withdraw funds (Headers: `Idempotency-Key`)
- `POST /api/transactions/transfer` - Transfer funds (Headers: `Idempotency-Key`)
- `GET /api/transactions` - Paginated transaction history for current user
- `GET /api/transactions/{id}` - Transaction details

### Compliance & Ledger Audit (`/api/ledger`, `/api/audit`)
- `GET /api/ledger/transaction/{transactionId}` - View balanced double-entry ledger lines
- `GET /api/ledger/reconciliation` - (Auditor/Admin) Live zero-sum equilibrium audit
- `GET /api/audit/logs` - (Auditor/Admin) Paginated chronological audit log trail

