package com.ledgerline.service;

import com.ledgerline.dto.response.LedgerReconciliationResponse;
import com.ledgerline.enums.EntryType;
import com.ledgerline.repository.FinancialTransactionRepository;
import com.ledgerline.repository.LedgerEntryRepository;
import com.ledgerline.service.impl.LedgerServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoubleEntryLedgerTest {

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @Mock
    private FinancialTransactionRepository transactionRepository;

    @InjectMocks
    private LedgerServiceImpl ledgerService;

    @Test
    @DisplayName("Ledger Reconciliation: Verified balanced when sum(Debits) equals sum(Credits)")
    void testReconciliationBalanced() {
        BigDecimal totalDebits = new BigDecimal("125000.00");
        BigDecimal totalCredits = new BigDecimal("125000.00");

        when(ledgerEntryRepository.sumAmountByEntryType(EntryType.DEBIT)).thenReturn(totalDebits);
        when(ledgerEntryRepository.sumAmountByEntryType(EntryType.CREDIT)).thenReturn(totalCredits);
        when(transactionRepository.count()).thenReturn(15L);
        when(ledgerEntryRepository.count()).thenReturn(30L);

        LedgerReconciliationResponse summary = ledgerService.getReconciliationSummary();

        assertNotNull(summary);
        assertTrue(summary.isBalanced());
        assertEquals(0, summary.getDifference().compareTo(BigDecimal.ZERO));
        assertEquals(0, totalDebits.compareTo(summary.getTotalDebits()));
        assertEquals(0, totalCredits.compareTo(summary.getTotalCredits()));
        assertEquals(15L, summary.getTotalTransactions());
        assertEquals(30L, summary.getTotalLedgerEntries());
    }

    @Test
    @DisplayName("Ledger Reconciliation: Detects imbalance if debits differ from credits")
    void testReconciliationImbalanced() {
        BigDecimal totalDebits = new BigDecimal("125000.00");
        BigDecimal totalCredits = new BigDecimal("120000.00");

        when(ledgerEntryRepository.sumAmountByEntryType(EntryType.DEBIT)).thenReturn(totalDebits);
        when(ledgerEntryRepository.sumAmountByEntryType(EntryType.CREDIT)).thenReturn(totalCredits);
        when(transactionRepository.count()).thenReturn(10L);
        when(ledgerEntryRepository.count()).thenReturn(20L);

        LedgerReconciliationResponse summary = ledgerService.getReconciliationSummary();

        assertNotNull(summary);
        assertFalse(summary.isBalanced());
        assertEquals(new BigDecimal("5000.00"), summary.getDifference());
    }
}

