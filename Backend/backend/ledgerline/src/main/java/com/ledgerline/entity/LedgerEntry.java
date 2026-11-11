package com.ledgerline.entity;

import com.ledgerline.enums.EntryType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ledger_entries", indexes = {
    @Index(name = "idx_ledger_transaction", columnList = "transaction_id"),
    @Index(name = "idx_ledger_account", columnList = "account_id"),
    @Index(name = "idx_ledger_account_created", columnList = "account_id, created_at"),
    @Index(name = "idx_ledger_entry_type", columnList = "entry_type")
})
public class LedgerEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private FinancialTransaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EntryType entryType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 120)
    private String accountLabel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public LedgerEntry() {}

    public LedgerEntry(FinancialTransaction transaction, Account account, EntryType entryType,
                       BigDecimal amount, String currency, String accountLabel) {
        this.transaction = transaction;
        this.account = account;
        this.entryType = entryType;
        this.amount = amount;
        this.currency = currency;
        this.accountLabel = accountLabel;
    }

    public boolean isDebit() {
        return this.entryType == EntryType.DEBIT;
    }

    public boolean isCredit() {
        return this.entryType == EntryType.CREDIT;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public FinancialTransaction getTransaction() { return transaction; }
    public void setTransaction(FinancialTransaction transaction) { this.transaction = transaction; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public EntryType getEntryType() { return entryType; }
    public void setEntryType(EntryType entryType) { this.entryType = entryType; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getAccountLabel() { return accountLabel; }
    public void setAccountLabel(String accountLabel) { this.accountLabel = accountLabel; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
