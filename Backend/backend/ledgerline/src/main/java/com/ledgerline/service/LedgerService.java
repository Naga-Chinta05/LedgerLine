package com.ledgerline.service;

import com.ledgerline.dto.response.LedgerEntryResponse;
import com.ledgerline.dto.response.LedgerReconciliationResponse;
import com.ledgerline.entity.Account;
import com.ledgerline.entity.FinancialTransaction;
import com.ledgerline.entity.LedgerEntry;
import com.ledgerline.enums.EntryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface LedgerService {
    LedgerEntry recordEntry(FinancialTransaction transaction, Account account, EntryType entryType,
                            BigDecimal amount, String currency, String accountLabel);
    List<LedgerEntryResponse> getEntriesForTransaction(Long transactionId);
    Page<LedgerEntryResponse> getEntriesForAccount(Long accountId, Pageable pageable);
    LedgerReconciliationResponse getReconciliationSummary();
}

