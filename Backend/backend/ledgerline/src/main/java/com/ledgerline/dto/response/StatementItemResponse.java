package com.ledgerline.dto.response;

import com.ledgerline.enums.EntryType;
import com.ledgerline.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public class StatementItemResponse {
    private Long transactionId;
    private Long ledgerEntryId;
    private Instant timestamp;
    private TransactionType transactionType;
    private EntryType entryType;
    private BigDecimal amount;
    private String description;
    private String accountLabel;
    private BigDecimal runningBalance;

    public StatementItemResponse() {}

    public StatementItemResponse(Long transactionId, Long ledgerEntryId, Instant timestamp,
                                 TransactionType transactionType, EntryType entryType,
                                 BigDecimal amount, String description, String accountLabel,
                                 BigDecimal runningBalance) {
        this.transactionId = transactionId;
        this.ledgerEntryId = ledgerEntryId;
        this.timestamp = timestamp;
        this.transactionType = transactionType;
        this.entryType = entryType;
        this.amount = amount;
        this.description = description;
        this.accountLabel = accountLabel;
        this.runningBalance = runningBalance;
    }

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public Long getLedgerEntryId() { return ledgerEntryId; }
    public void setLedgerEntryId(Long ledgerEntryId) { this.ledgerEntryId = ledgerEntryId; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public EntryType getEntryType() { return entryType; }
    public void setEntryType(EntryType entryType) { this.entryType = entryType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAccountLabel() { return accountLabel; }
    public void setAccountLabel(String accountLabel) { this.accountLabel = accountLabel; }

    public BigDecimal getRunningBalance() { return runningBalance; }
    public void setRunningBalance(BigDecimal runningBalance) { this.runningBalance = runningBalance; }
}

