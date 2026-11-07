package com.ledgerline.dto.response;

import com.ledgerline.enums.TransactionStatus;
import com.ledgerline.enums.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class TransactionResponse {
    private Long id;
    private TransactionType type;
    private TransactionStatus status;
    private BigDecimal amount;
    private String currency;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private String description;
    private Long createdBy;
    private Instant createdAt;
    private List<LedgerEntryResponse> ledgerEntries;

    public TransactionResponse() {}

    public TransactionResponse(Long id, TransactionType type, TransactionStatus status, BigDecimal amount,
                               String currency, String sourceAccountNumber, String destinationAccountNumber,
                               String description, Long createdBy, Instant createdAt, List<LedgerEntryResponse> ledgerEntries) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
        this.sourceAccountNumber = sourceAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.ledgerEntries = ledgerEntries;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getSourceAccountNumber() { return sourceAccountNumber; }
    public void setSourceAccountNumber(String sourceAccountNumber) { this.sourceAccountNumber = sourceAccountNumber; }
    public String getDestinationAccountNumber() { return destinationAccountNumber; }
    public void setDestinationAccountNumber(String destinationAccountNumber) { this.destinationAccountNumber = destinationAccountNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public List<LedgerEntryResponse> getLedgerEntries() { return ledgerEntries; }
    public void setLedgerEntries(List<LedgerEntryResponse> ledgerEntries) { this.ledgerEntries = ledgerEntries; }
}

