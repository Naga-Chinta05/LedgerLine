package com.ledgerline.service;

import com.ledgerline.dto.request.CreateAccountRequest;
import com.ledgerline.dto.response.AccountResponse;
import com.ledgerline.dto.response.AccountStatementResponse;
import com.ledgerline.dto.response.SystemSummaryResponse;
import com.ledgerline.enums.AccountStatus;
import com.ledgerline.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface AccountService {
    AccountResponse createAccount(Long ownerId, CreateAccountRequest request, String ipAddress);
    List<AccountResponse> getAccountsByOwner(Long ownerId);
    AccountResponse getAccountById(Long accountId);
    AccountResponse getAccountByNumber(String accountNumber);
    AccountResponse updateAccountStatus(Long accountId, AccountStatus status, String reason, Long adminUserId, String ipAddress);
    Page<AccountResponse> getAllAccounts(Pageable pageable);
    AccountStatementResponse getAccountStatement(Long accountId, Instant startDate, Instant endDate, UserPrincipal principal);
    SystemSummaryResponse getSystemSummary();
}
