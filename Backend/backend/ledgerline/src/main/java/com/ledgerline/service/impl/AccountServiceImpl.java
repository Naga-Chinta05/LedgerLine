package com.ledgerline.service.impl;

import com.ledgerline.dto.request.CreateAccountRequest;
import com.ledgerline.dto.response.AccountResponse;
import com.ledgerline.dto.response.AccountStatementResponse;
import com.ledgerline.dto.response.StatementItemResponse;
import com.ledgerline.dto.response.SystemSummaryResponse;
import com.ledgerline.entity.Account;
import com.ledgerline.entity.LedgerEntry;
import com.ledgerline.entity.User;
import com.ledgerline.enums.AccountStatus;
import com.ledgerline.enums.EntryType;
import com.ledgerline.enums.TransactionStatus;
import com.ledgerline.enums.TransactionType;
import com.ledgerline.enums.UserRole;
import com.ledgerline.exception.AccountNotFoundException;
import com.ledgerline.exception.InvalidTransactionException;
import com.ledgerline.exception.ResourceNotFoundException;
import com.ledgerline.exception.UnauthorizedAccessException;
import com.ledgerline.repository.AccountRepository;
import com.ledgerline.repository.FinancialTransactionRepository;
import com.ledgerline.repository.LedgerEntryRepository;
import com.ledgerline.repository.UserRepository;
import com.ledgerline.security.UserPrincipal;
import com.ledgerline.service.AccountService;
import com.ledgerline.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final FinancialTransactionRepository transactionRepository;
    private final AuditService auditService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AccountServiceImpl(AccountRepository accountRepository,
                              UserRepository userRepository,
                              LedgerEntryRepository ledgerEntryRepository,
                              FinancialTransactionRepository transactionRepository,
                              AuditService auditService) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.transactionRepository = transactionRepository;
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

    @Override
    @Transactional(readOnly = true)
    public AccountStatementResponse getAccountStatement(Long accountId, Instant startDate, Instant endDate, UserPrincipal principal) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        // Authorization check: owner, admin, or auditor
        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.AUDITOR) {
            if (!account.getOwner().getId().equals(principal.getId())) {
                throw new UnauthorizedAccessException("You are not authorized to view statements for this account.");
            }
        }

        if (endDate == null) {
            endDate = Instant.now();
        }
        if (startDate == null) {
            startDate = endDate.minus(30, ChronoUnit.DAYS);
        }
        if (startDate.isAfter(endDate)) {
            throw new InvalidTransactionException("Start date cannot be after end date.");
        }

        // Opening balance calculation: prior credits minus prior debits before periodStart
        BigDecimal priorCredits = ledgerEntryRepository.sumAmountByAccountAndEntryTypeBefore(accountId, EntryType.CREDIT, startDate);
        BigDecimal priorDebits = ledgerEntryRepository.sumAmountByAccountAndEntryTypeBefore(accountId, EntryType.DEBIT, startDate);
        BigDecimal openingBalance = priorCredits.subtract(priorDebits);
        if (openingBalance.compareTo(BigDecimal.ZERO) < 0) {
            openingBalance = BigDecimal.ZERO;
        }

        List<LedgerEntry> entries = ledgerEntryRepository.findByAccountIdAndDateRange(accountId, startDate, endDate);

        BigDecimal runningBalance = openingBalance;
        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;
        List<StatementItemResponse> statementItems = new ArrayList<>();

        for (LedgerEntry entry : entries) {
            if (entry.getEntryType() == EntryType.CREDIT) {
                runningBalance = runningBalance.add(entry.getAmount());
                totalCredits = totalCredits.add(entry.getAmount());
            } else {
                runningBalance = runningBalance.subtract(entry.getAmount());
                totalDebits = totalDebits.add(entry.getAmount());
            }

            statementItems.add(new StatementItemResponse(
                    entry.getTransaction().getId(),
                    entry.getId(),
                    entry.getCreatedAt(),
                    entry.getTransaction().getType(),
                    entry.getEntryType(),
                    entry.getAmount(),
                    entry.getTransaction().getDescription(),
                    entry.getAccountLabel(),
                    runningBalance
            ));
        }

        AccountStatementResponse response = new AccountStatementResponse();
        response.setAccountId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountStatus(account.getStatus());
        response.setCurrency(account.getCurrency());
        response.setOwnerName(account.getOwner().getFullName());
        response.setOwnerEmail(account.getOwner().getEmail());
        response.setPeriodStart(startDate);
        response.setPeriodEnd(endDate);
        response.setGeneratedAt(Instant.now());
        response.setOpeningBalance(openingBalance);
        response.setClosingBalance(runningBalance);
        response.setTotalDebits(totalDebits);
        response.setTotalCredits(totalCredits);
        response.setNetCashFlow(totalCredits.subtract(totalDebits));
        response.setTransactionCount(statementItems.size());
        response.setEntries(statementItems);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public SystemSummaryResponse getSystemSummary() {
        SystemSummaryResponse summary = new SystemSummaryResponse();
        summary.setTimestamp(Instant.now());

        // Users distribution
        summary.setTotalUsers(userRepository.count());
        Map<String, Long> usersByRole = new HashMap<>();
        for (UserRole role : UserRole.values()) {
            usersByRole.put(role.name(), userRepository.countByRole(role));
        }
        summary.setUsersByRole(usersByRole);

        // Accounts distribution
        summary.setTotalAccounts(accountRepository.count());
        Map<String, Long> accountsByStatus = new HashMap<>();
        for (AccountStatus status : AccountStatus.values()) {
            accountsByStatus.put(status.name(), accountRepository.countByStatus(status));
        }
        summary.setAccountsByStatus(accountsByStatus);
        summary.setTotalSystemLiquidity(accountRepository.sumTotalLiquidity());

        // Transactions distribution
        summary.setTotalTransactions(transactionRepository.count());
        Map<String, Long> txByType = new HashMap<>();
        for (TransactionType type : TransactionType.values()) {
            txByType.put(type.name(), transactionRepository.countByType(type));
        }
        summary.setTransactionsByType(txByType);

        Map<String, Long> txByStatus = new HashMap<>();
        for (TransactionStatus status : TransactionStatus.values()) {
            txByStatus.put(status.name(), transactionRepository.countByStatus(status));
        }
        summary.setTransactionsByStatus(txByStatus);

        // Ledger Reconciliation metrics
        BigDecimal totalDebits = ledgerEntryRepository.sumAmountByEntryType(EntryType.DEBIT);
        BigDecimal totalCredits = ledgerEntryRepository.sumAmountByEntryType(EntryType.CREDIT);
        BigDecimal variance = totalDebits.subtract(totalCredits).abs();
        summary.setTotalDebits(totalDebits);
        summary.setTotalCredits(totalCredits);
        summary.setLedgerVariance(variance);
        summary.setReconciled(variance.compareTo(BigDecimal.ZERO) == 0);

        return summary;
    }

    private String generateAccountNumber() {
        String accNum;
        do {
            long number = 1000000000L + (long)(secureRandom.nextDouble() * 9000000000L);
            accNum = "LL-" + number;
        } while (accountRepository.existsByAccountNumber(accNum));
        return accNum;
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
}
