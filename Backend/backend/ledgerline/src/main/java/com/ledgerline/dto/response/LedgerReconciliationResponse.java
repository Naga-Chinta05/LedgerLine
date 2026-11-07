package com.ledgerline.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public class LedgerReconciliationResponse {
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
    private BigDecimal difference;
    private boolean balanced;
    private long totalTransactions;
    private long totalLedgerEntries;
    private Instant verifiedAt;

    public LedgerReconciliationResponse() {}

    public LedgerReconciliationResponse(BigDecimal totalDebits, BigDecimal totalCredits, BigDecimal difference,
                                        boolean balanced, long totalTransactions, long totalLedgerEntries, Instant verifiedAt) {
        this.totalDebits = totalDebits;
        this.totalCredits = totalCredits;
        this.difference = difference;
        this.balanced = balanced;
        this.totalTransactions = totalTransactions;
        this.totalLedgerEntries = totalLedgerEntries;
        this.verifiedAt = verifiedAt;
    }

    public BigDecimal getTotalDebits() { return totalDebits; }
    public void setTotalDebits(BigDecimal totalDebits) { this.totalDebits = totalDebits; }
    public BigDecimal getTotalCredits() { return totalCredits; }
    public void setTotalCredits(BigDecimal totalCredits) { this.totalCredits = totalCredits; }
    public BigDecimal getDifference() { return difference; }
    public void setDifference(BigDecimal difference) { this.difference = difference; }
    public boolean isBalanced() { return balanced; }
    public void setBalanced(boolean balanced) { this.balanced = balanced; }
    public long getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(long totalTransactions) { this.totalTransactions = totalTransactions; }
    public long getTotalLedgerEntries() { return totalLedgerEntries; }
    public void setTotalLedgerEntries(long totalLedgerEntries) { this.totalLedgerEntries = totalLedgerEntries; }
    public Instant getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Instant verifiedAt) { this.verifiedAt = verifiedAt; }
}

