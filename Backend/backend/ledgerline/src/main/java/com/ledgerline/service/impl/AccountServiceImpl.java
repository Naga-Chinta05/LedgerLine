package com.ledgerline.service.impl;

import com.ledgerline.dto.request.CreateAccountRequest;
import com.ledgerline.dto.response.AccountResponse;
import com.ledgerline.entity.Account;
import com.ledgerline.entity.User;
import com.ledgerline.enums.AccountStatus;
import com.ledgerline.exception.AccountNotFoundException;
import com.ledgerline.exception.ResourceNotFoundException;
import com.ledgerline.repository.AccountRepository;
import com.ledgerline.repository.UserRepository;
import com.ledgerline.service.AccountService;
import com.ledgerline.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AccountServiceImpl(AccountRepository accountRepository,
                              UserRepository userRepository,
                              AuditService auditService) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public AccountResponse createAccount(Long ownerId, CreateAccountRequest request, String ipAddress) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + ownerId));

        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setOwner(owner);
        account.setBalance(BigDecimal.ZERO);
        account.setCurrency(request.getCurrency() != null ? request.getCurrency() : "INR");
        account.setStatus(AccountStatus.ACTIVE);

        Account saved = accountRepository.save(account);

        auditService.logAction(
                ownerId,
                "CREATE_ACCOUNT",
                "ACCOUNT",
                saved.getId(),
                "Created account " + saved.getAccountNumber() + " with currency " + saved.getCurrency(),
                ipAddress
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByOwner(Long ownerId) {
        return accountRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        return mapToResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        return mapToResponse(account);
    }

    @Override
    @Transactional
    public AccountResponse updateAccountStatus(Long accountId, AccountStatus status, String reason, Long adminUserId, String ipAddress) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        AccountStatus oldStatus = account.getStatus();
        account.setStatus(status);
        Account saved = accountRepository.save(account);

        auditService.logAction(
                adminUserId,
                "UPDATE_ACCOUNT_STATUS",
                "ACCOUNT",
                saved.getId(),
                String.format("Status changed from %s to %s. Reason: %s", oldStatus, status, reason != null ? reason : "N/A"),
                ipAddress
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountResponse> getAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable).map(this::mapToResponse);
    }

    private AccountResponse mapToResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getOwner().getId(),
                account.getOwner().getFullName(),
                account.getOwner().getEmail(),
                account.getBalance(),
                account.getCurrency(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    private String generateAccountNumber() {
        String accNum;
        do {
            long number = 1000000000L + (long)(secureRandom.nextDouble() * 9000000000L);
            accNum = "LL-" + number;
        } while (accountRepository.existsByAccountNumber(accNum));
        return accNum;
    }
}

