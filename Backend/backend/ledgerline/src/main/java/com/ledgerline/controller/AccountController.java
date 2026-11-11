package com.ledgerline.controller;

import com.ledgerline.dto.request.CreateAccountRequest;
import com.ledgerline.dto.request.UpdateAccountStatusRequest;
import com.ledgerline.dto.response.AccountResponse;
import com.ledgerline.dto.response.AccountStatementResponse;
import com.ledgerline.dto.response.SystemSummaryResponse;
import com.ledgerline.enums.UserRole;
import com.ledgerline.exception.UnauthorizedAccessException;
import com.ledgerline.security.UserPrincipal;
import com.ledgerline.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request,
                                                         @AuthenticationPrincipal UserPrincipal principal,
                                                         HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        AccountResponse response = accountService.createAccount(principal.getId(), request, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountResponse>> getMyAccounts(@AuthenticationPrincipal UserPrincipal principal) {
        List<AccountResponse> accounts = accountService.getAccountsByOwner(principal.getId());
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id,
                                                          @AuthenticationPrincipal UserPrincipal principal) {
        AccountResponse response = accountService.getAccountById(id);
        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.AUDITOR) {
            if (!response.getOwnerId().equals(principal.getId())) {
                throw new UnauthorizedAccessException("You are not authorized to view this account.");
            }
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts/number/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccountByNumber(@PathVariable String accountNumber) {
        // Public/Authenticated lookup for verifying destination recipient name during transfers
        AccountResponse response = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts/{id}/statement")
    public ResponseEntity<AccountStatementResponse> getAccountStatement(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @AuthenticationPrincipal UserPrincipal principal) {
        AccountStatementResponse response = accountService.getAccountStatement(id, startDate, endDate, principal);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/accounts/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponse> updateAccountStatus(@PathVariable Long id,
                                                               @Valid @RequestBody UpdateAccountStatusRequest request,
                                                               @AuthenticationPrincipal UserPrincipal principal,
                                                               HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        AccountResponse response = accountService.updateAccountStatus(id, request.getStatus(), request.getReason(), principal.getId(), ipAddress);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/accounts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AccountResponse>> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AccountResponse> accounts = accountService.getAllAccounts(pageable);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/admin/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public ResponseEntity<SystemSummaryResponse> getSystemSummary() {
        SystemSummaryResponse summary = accountService.getSystemSummary();
        return ResponseEntity.ok(summary);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(",")) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
