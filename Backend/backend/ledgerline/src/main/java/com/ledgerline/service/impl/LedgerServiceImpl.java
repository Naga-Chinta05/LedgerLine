package com.ledgerline.service.impl;

import com.ledgerline.dto.response.LedgerEntryResponse;
import com.ledgerline.dto.response.LedgerReconciliationResponse;
import com.ledgerline.entity.Account;
import com.ledgerline.entity.FinancialTransaction;
import com.ledgerline.entity.LedgerEntry;
import com.ledgerline.enums.EntryType;
import com.ledgerline.repository.FinancialTransactionRepository;
import com.ledgerline.repository.LedgerEntryRepository;
import com.ledgerline.service.LedgerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class LedgerServiceImpl implements LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final FinancialTransactionRepository transactionRepository;

    public LedgerServiceImpl(LedgerEntryRepository ledgerEntryRepository,
                             FinancialTransactionRepository transactionRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public LedgerEntry recordEntry(FinancialTransaction transaction, Account account, EntryType entryType,
                                   BigDecimal amount, String currency, String accountLabel) {
        LedgerEntry entry = new LedgerEntry();
        entry.setTransaction(transaction);
        entry.setAccount(account);
        entry.setEntryType(entryType);
        entry.setAmount(amount);
        entry.setCurrency(currency);
        entry.setAccountLabel(accountLabel);
        return ledgerEntryRepository.save(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LedgerEntryResponse> getEntriesForTransaction(Long transactionId) {
        return ledgerEntryRepository.findByTransactionIdOrderByIdAsc(transactionId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LedgerEntryResponse> getEntriesForAccount(Long accountId, Pageable pageable) {
        return ledgerEntryRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public LedgerReconciliationResponse getReconciliationSummary() {
        BigDecimal totalDebits = ledgerEntryRepository.sumAmountByEntryType(EntryType.DEBIT);
        BigDecimal totalCredits = ledgerEntryRepository.sumAmountByEntryType(EntryType.CREDIT);

        if (totalDebits == null) totalDebits = BigDecimal.ZERO;
        if (totalCredits == null) totalCredits = BigDecimal.ZERO;

        BigDecimal diff = totalDebits.subtract(totalCredits).abs();
        boolean balanced = diff.compareTo(BigDecimal.ZERO) == 0;
        long totalTransactions = transactionRepository.count();
        long totalLedgerEntries = ledgerEntryRepository.count();

        return new LedgerReconciliationResponse(
                totalDebits,
                totalCredits,
                diff,
                balanced,
                totalTransactions,
                totalLedgerEntries,
                Instant.now()
        );
    }

    private LedgerEntryResponse mapToResponse(LedgerEntry entry) {
        return new LedgerEntryResponse(
                entry.getId(),
                entry.getTransaction().getId(),
                entry.getAccount() != null ? entry.getAccount().getId() : null,
                entry.getAccount() != null ? entry.getAccount().getAccountNumber() : "EXTERNAL / SYSTEM",
                entry.getEntryType(),
                entry.getAmount(),
                entry.getCurrency(),
                entry.getAccountLabel(),
                entry.getCreatedAt()
        );
    }
}

