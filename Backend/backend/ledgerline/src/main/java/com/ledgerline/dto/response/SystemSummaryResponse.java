package com.ledgerline.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public class SystemSummaryResponse {
    private long totalUsers;
    private Map<String, Long> usersByRole;
    private long totalAccounts;
    private Map<String, Long> accountsByStatus;
    private BigDecimal totalSystemLiquidity;
    private long totalTransactions;
    private Map<String, Long> transactionsByType;
    private Map<String, Long> transactionsByStatus;
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
    private BigDecimal ledgerVariance;
    private boolean isReconciled;
    private Instant timestamp;

    public SystemSummaryResponse() {}

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public Map<String, Long> getUsersByRole() { return usersByRole; }
    public void setUsersByRole(Map<String, Long> usersByRole) { this.usersByRole = usersByRole; }

    public long getTotalAccounts() { return totalAccounts; }
    public void setTotalAccounts(long totalAccounts) { this.totalAccounts = totalAccounts; }

    public Map<String, Long> getAccountsByStatus() { return accountsByStatus; }
    public void setAccountsByStatus(Map<String, Long> accountsByStatus) { this.accountsByStatus = accountsByStatus; }

    public BigDecimal getTotalSystemLiquidity() { return totalSystemLiquidity; }
    public void setTotalSystemLiquidity(BigDecimal totalSystemLiquidity) { this.totalSystemLiquidity = totalSystemLiquidity; }

    public long getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(long totalTransactions) { this.totalTransactions = totalTransactions; }

    public Map<String, Long> getTransactionsByType() { return transactionsByType; }
    public void setTransactionsByType(Map<String, Long> transactionsByType) { this.transactionsByType = transactionsByType; }

    public Map<String, Long> getTransactionsByStatus() { return transactionsByStatus; }
    public void setTransactionsByStatus(Map<String, Long> transactionsByStatus) { this.transactionsByStatus = transactionsByStatus; }

    public BigDecimal getTotalDebits() { return totalDebits; }
    public void setTotalDebits(BigDecimal totalDebits) { this.totalDebits = totalDebits; }

    public BigDecimal getTotalCredits() { return totalCredits; }
    public void setTotalCredits(BigDecimal totalCredits) { this.totalCredits = totalCredits; }

    public BigDecimal getLedgerVariance() { return ledgerVariance; }
    public void setLedgerVariance(BigDecimal ledgerVariance) { this.ledgerVariance = ledgerVariance; }

    public boolean isReconciled() { return isReconciled; }
    public void setReconciled(boolean reconciled) { isReconciled = reconciled; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}

