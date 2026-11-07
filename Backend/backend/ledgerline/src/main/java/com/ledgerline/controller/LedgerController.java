package com.ledgerline.controller;

import com.ledgerline.dto.response.LedgerEntryResponse;
import com.ledgerline.dto.response.LedgerReconciliationResponse;
import com.ledgerline.service.LedgerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<List<LedgerEntryResponse>> getEntriesForTransaction(@PathVariable Long transactionId) {
        List<LedgerEntryResponse> entries = ledgerService.getEntriesForTransaction(transactionId);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<Page<LedgerEntryResponse>> getEntriesForAccount(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LedgerEntryResponse> entries = ledgerService.getEntriesForAccount(accountId, pageable);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/reconciliation")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    public ResponseEntity<LedgerReconciliationResponse> getReconciliationSummary() {
        LedgerReconciliationResponse summary = ledgerService.getReconciliationSummary();
        return ResponseEntity.ok(summary);
    }
}

