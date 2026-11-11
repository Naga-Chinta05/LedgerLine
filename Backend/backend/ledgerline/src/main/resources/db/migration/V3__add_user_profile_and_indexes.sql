-- Flyway migration V3: Add user profile fields and database performance indexes for ledger operations

ALTER TABLE users ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;

-- Indexes for Account queries
CREATE INDEX IF NOT EXISTS idx_accounts_owner_id ON accounts(owner_id);
CREATE INDEX IF NOT EXISTS idx_accounts_status ON accounts(status);

-- Indexes for Transaction queries
CREATE INDEX IF NOT EXISTS idx_tx_source_acc ON transactions(source_account_id);
CREATE INDEX IF NOT EXISTS idx_tx_dest_acc ON transactions(destination_account_id);
CREATE INDEX IF NOT EXISTS idx_tx_created_by ON transactions(created_by);
CREATE INDEX IF NOT EXISTS idx_tx_created_at ON transactions(created_at);
CREATE INDEX IF NOT EXISTS idx_tx_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_tx_type ON transactions(type);

-- Indexes for Double-Entry Ledger statements & reconciliation
CREATE INDEX IF NOT EXISTS idx_ledger_account_created ON ledger_entries(account_id, created_at);
CREATE INDEX IF NOT EXISTS idx_ledger_entry_type ON ledger_entries(entry_type);

-- Indexes for Audit and Compliance queries
CREATE INDEX IF NOT EXISTS idx_audit_actor ON audit_logs(actor_user_id);
CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit_logs(entity_type, entity_id);

-- Indexes for Idempotency lookups
CREATE INDEX IF NOT EXISTS idx_idempotency_created_at ON idempotency_records(created_at);

