package com.ledgerline.service.impl;

import com.ledgerline.dto.request.DepositRequest;
import com.ledgerline.dto.request.TransferRequest;
import com.ledgerline.dto.request.WithdrawalRequest;
import com.ledgerline.dto.response.LedgerEntryResponse;
import com.ledgerline.dto.response.TransactionResponse;
import com.ledgerline.entity.Account;
import com.ledgerline.entity.FinancialTransaction;
import com.ledgerline.entity.IdempotencyRecord;
import com.ledgerline.enums.AccountStatus;
import com.ledgerline.enums.EntryType;
import com.ledgerline.enums.TransactionStatus;
import com.ledgerline.enums.TransactionType;
import com.ledgerline.enums.UserRole;
import com.ledgerline.exception.*;
import com.ledgerline.repository.AccountRepository;
import com.ledgerline.repository.FinancialTransactionRepository;
import com.ledgerline.security.UserPrincipal;
import com.ledgerline.service.AuditService;
import com.ledgerline.service.IdempotencyService;
import com.ledgerline.service.LedgerService;
import com.ledgerline.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final FinancialTransactionRepository transactionRepository;
    private final LedgerService ledgerService;
    private final IdempotencyService idempotencyService;
    private final AuditService auditService;

    public TransactionServiceImpl(AccountRepository accountRepository,
                                  FinancialTransactionRepository transactionRepository,
                                  LedgerService ledgerService,
                                  IdempotencyService idempotencyService,
                                  AuditService auditService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerService = ledgerService;
        this.idempotencyService = idempotencyService;
        this.auditService = auditService;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionResponse deposit(DepositRequest request, String idempotencyKey, UserPrincipal principal, String ipAddress) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Deposit amount must be strictly greater than 0");
        }

        String requestHash = idempotencyService.computeHash("DEPOSIT", request.getAccountNumber(), request.getAmount().toPlainString());
        Optional<TransactionResponse> cached = idempotencyService.getCompletedResponse(principal.getId(), idempotencyKey, requestHash);
        if (cached.isPresent()) {
            return cached.get();
        }

        IdempotencyRecord idempotencyRecord = null;
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyRecord = idempotencyService.createProcessingRecord(principal.getId(), idempotencyKey, requestHash);
        }

        try {
            Account account = accountRepository.findByAccountNumberWithLock(request.getAccountNumber())
                    .orElseThrow(() -> new AccountNotFoundException(request.getAccountNumber()));

            if (account.getStatus() != AccountStatus.ACTIVE) {
                throw new AccountInactiveException(
                        String.format("Account %s is %s and cannot accept deposits.", account.getAccountNumber(), account.getStatus()));
            }

            // Update balance
            account.setBalance(account.getBalance().add(request.getAmount()));
            accountRepository.save(account);

            // Record transaction
            FinancialTransaction tx = new FinancialTransaction();
            tx.setType(TransactionType.DEPOSIT);
            tx.setStatus(TransactionStatus.POSTED);
            tx.setAmount(request.getAmount());
            tx.setCurrency(account.getCurrency());
            tx.setDestinationAccount(account);
            tx.setDescription(request.getDescription());
            tx.setCreatedBy(principal.getId());
            FinancialTransaction savedTx = transactionRepository.save(tx);

            // Double-entry ledger: Debit Central Reserve, Credit Account Holder
            ledgerService.recordEntry(savedTx, null, EntryType.DEBIT, request.getAmount(), account.getCurrency(), "CENTRAL_CASH_RESERVE");
            ledgerService.recordEntry(savedTx, account, EntryType.CREDIT, request.getAmount(), account.getCurrency(), "CUSTOMER_ACCOUNT: " + account.getAccountNumber());

            auditService.logAction(
                    principal.getId(),
                    "DEPOSIT",
                    "TRANSACTION",
                    savedTx.getId(),
                    String.format("Deposited %s %s into account %s", request.getAmount(), account.getCurrency(), account.getAccountNumber()),
                    ipAddress
            );

            TransactionResponse response = mapToResponse(savedTx);

            if (idempotencyRecord != null) {
                idempotencyService.completeRecord(idempotencyRecord, response);
            }

            return response;
        } catch (Exception ex) {
            if (idempotencyRecord != null) {
                idempotencyService.failRecord(idempotencyRecord);
            }
            throw ex;
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionResponse withdraw(WithdrawalRequest request, String idempotencyKey, UserPrincipal principal, String ipAddress) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Withdrawal amount must be strictly greater than 0");
        }

        String requestHash = idempotencyService.computeHash("WITHDRAWAL", request.getAccountNumber(), request.getAmount().toPlainString());
        Optional<TransactionResponse> cached = idempotencyService.getCompletedResponse(principal.getId(), idempotencyKey, requestHash);
        if (cached.isPresent()) {
            return cached.get();
        }

        IdempotencyRecord idempotencyRecord = null;
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyRecord = idempotencyService.createProcessingRecord(principal.getId(), idempotencyKey, requestHash);
        }

        try {
            Account account = accountRepository.findByAccountNumberWithLock(request.getAccountNumber())
                    .orElseThrow(() -> new AccountNotFoundException(request.getAccountNumber()));

            // Authorization: only the owner or an ADMIN can withdraw
            if (principal.getRole() != UserRole.ADMIN && !account.getOwner().getId().equals(principal.getId())) {
                throw new UnauthorizedAccessException("You are not authorized to withdraw from this account.");
            }

            if (account.getStatus() != AccountStatus.ACTIVE) {
                throw new AccountInactiveException(
                        String.format("Account %s is %s and cannot be debited.", account.getAccountNumber(), account.getStatus()));
            }

            if (account.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientFundsException(account.getAccountNumber(), account.getBalance(), request.getAmount());
            }

            // Deduct balance
            account.setBalance(account.getBalance().subtract(request.getAmount()));
            accountRepository.save(account);

            // Record transaction
            FinancialTransaction tx = new FinancialTransaction();
            tx.setType(TransactionType.WITHDRAWAL);
            tx.setStatus(TransactionStatus.POSTED);
            tx.setAmount(request.getAmount());
            tx.setCurrency(account.getCurrency());
            tx.setSourceAccount(account);
            tx.setDescription(request.getDescription());
            tx.setCreatedBy(principal.getId());
            FinancialTransaction savedTx = transactionRepository.save(tx);

            // Double-entry ledger: Debit Account Holder, Credit Cash Disbursement Outlet
            ledgerService.recordEntry(savedTx, account, EntryType.DEBIT, request.getAmount(), account.getCurrency(), "CUSTOMER_ACCOUNT: " + account.getAccountNumber());
            ledgerService.recordEntry(savedTx, null, EntryType.CREDIT, request.getAmount(), account.getCurrency(), "ATM_CASH_DISBURSEMENT");

            auditService.logAction(
                    principal.getId(),
                    "WITHDRAWAL",
                    "TRANSACTION",
                    savedTx.getId(),
                    String.format("Withdrew %s %s from account %s", request.getAmount(), account.getCurrency(), account.getAccountNumber()),
                    ipAddress
            );

            TransactionResponse response = mapToResponse(savedTx);

            if (idempotencyRecord != null) {
                idempotencyService.completeRecord(idempotencyRecord, response);
            }

            return response;
        } catch (Exception ex) {
            if (idempotencyRecord != null) {
                idempotencyService.failRecord(idempotencyRecord);
            }
            throw ex;
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionResponse transfer(TransferRequest request, String idempotencyKey, UserPrincipal principal, String ipAddress) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Transfer amount must be strictly greater than 0");
        }

        if (request.getSourceAccountNumber().trim().equalsIgnoreCase(request.getDestinationAccountNumber().trim())) {
            throw new InvalidTransactionException("Source and destination accounts must be distinct.");
        }

        String requestHash = idempotencyService.computeHash("TRANSFER", request.getSourceAccountNumber(), request.getDestinationAccountNumber(), request.getAmount().toPlainString());
        Optional<TransactionResponse> cached = idempotencyService.getCompletedResponse(principal.getId(), idempotencyKey, requestHash);
        if (cached.isPresent()) {
            return cached.get();
        }

        IdempotencyRecord idempotencyRecord = null;
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyRecord = idempotencyService.createProcessingRecord(principal.getId(), idempotencyKey, requestHash);
        }

        try {
            // Find IDs without lock first to determine lock ordering
            Account srcUnchecked = accountRepository.findByAccountNumber(request.getSourceAccountNumber().trim())
                    .orElseThrow(() -> new AccountNotFoundException("Source account " + request.getSourceAccountNumber()));
            Account dstUnchecked = accountRepository.findByAccountNumber(request.getDestinationAccountNumber().trim())
                    .orElseThrow(() -> new AccountNotFoundException("Destination account " + request.getDestinationAccountNumber()));

            // Deadlock Prevention: strictly lock accounts in ascending order of primary key
            Long firstId = Math.min(srcUnchecked.getId(), dstUnchecked.getId());
            Long secondId = Math.max(srcUnchecked.getId(), dstUnchecked.getId());

            Account firstLocked = accountRepository.findByIdWithLock(firstId)
                    .orElseThrow(() -> new AccountNotFoundException(firstId));
            Account secondLocked = accountRepository.findByIdWithLock(secondId)
                    .orElseThrow(() -> new AccountNotFoundException(secondId));

            Account sourceAccount = srcUnchecked.getId().equals(firstId) ? firstLocked : secondLocked;
            Account destAccount = dstUnchecked.getId().equals(firstId) ? firstLocked : secondLocked;

            // Authorization: sender must own the source account or be ADMIN
            if (principal.getRole() != UserRole.ADMIN && !sourceAccount.getOwner().getId().equals(principal.getId())) {
                throw new UnauthorizedAccessException("You are not authorized to transfer funds from account " + sourceAccount.getAccountNumber());
            }

            // Status checks
            if (sourceAccount.getStatus() != AccountStatus.ACTIVE) {
                throw new AccountInactiveException(String.format("Source account %s is %s.", sourceAccount.getAccountNumber(), sourceAccount.getStatus()));
            }
            if (destAccount.getStatus() != AccountStatus.ACTIVE) {
                throw new AccountInactiveException(String.format("Destination account %s is %s.", destAccount.getAccountNumber(), destAccount.getStatus()));
            }

            // Currency check
            if (!sourceAccount.getCurrency().equalsIgnoreCase(destAccount.getCurrency())) {
                throw new InvalidTransactionException(String.format("Currency mismatch: Source is %s but destination is %s",
                        sourceAccount.getCurrency(), destAccount.getCurrency()));
            }

            // Balance check
            if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientFundsException(sourceAccount.getAccountNumber(), sourceAccount.getBalance(), request.getAmount());
            }

            // Transfer money
            sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
            destAccount.setBalance(destAccount.getBalance().add(request.getAmount()));

            accountRepository.save(sourceAccount);
            accountRepository.save(destAccount);

            // Record transaction
            FinancialTransaction tx = new FinancialTransaction();
            tx.setType(TransactionType.TRANSFER);
            tx.setStatus(TransactionStatus.POSTED);
            tx.setAmount(request.getAmount());
            tx.setCurrency(sourceAccount.getCurrency());
            tx.setSourceAccount(sourceAccount);
            tx.setDestinationAccount(destAccount);
            tx.setDescription(request.getDescription());
            tx.setCreatedBy(principal.getId());
            FinancialTransaction savedTx = transactionRepository.save(tx);

            // Double-entry ledger: Debit Source, Credit Destination
            ledgerService.recordEntry(savedTx, sourceAccount, EntryType.DEBIT, request.getAmount(), sourceAccount.getCurrency(),
                    "TRANSFER_OUT: " + sourceAccount.getAccountNumber());
            ledgerService.recordEntry(savedTx, destAccount, EntryType.CREDIT, request.getAmount(), destAccount.getCurrency(),
                    "TRANSFER_IN: " + destAccount.getAccountNumber());

            auditService.logAction(
                    principal.getId(),
                    "TRANSFER",
                    "TRANSACTION",
                    savedTx.getId(),
                    String.format("Transferred %s %s from %s to %s",
                            request.getAmount(), sourceAccount.getCurrency(), sourceAccount.getAccountNumber(), destAccount.getAccountNumber()),
                    ipAddress
            );

            TransactionResponse response = mapToResponse(savedTx);

            if (idempotencyRecord != null) {
                idempotencyService.completeRecord(idempotencyRecord, response);
            }

            return response;
        } catch (Exception ex) {
            if (idempotencyRecord != null) {
                idempotencyService.failRecord(idempotencyRecord);
            }
            throw ex;
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionResponse reverseTransaction(Long transactionId, String reason, UserPrincipal principal, String ipAddress) {
        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.AUDITOR) {
            throw new UnauthorizedAccessException("Only Administrators and Auditors can reverse financial transactions.");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new InvalidTransactionException("A reversal reason must be provided for audit compliance.");
        }

        FinancialTransaction originalTx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));

        if (originalTx.getStatus() != TransactionStatus.POSTED) {
            throw new InvalidTransactionException(String.format("Cannot reverse transaction #%d because status is %s (only POSTED transactions can be reversed).",
                    transactionId, originalTx.getStatus()));
        }

        if (originalTx.getType() == TransactionType.DEPOSIT) {
            Account dest = accountRepository.findByAccountNumberWithLock(originalTx.getDestinationAccount().getAccountNumber())
                    .orElseThrow(() -> new AccountNotFoundException(originalTx.getDestinationAccount().getAccountNumber()));

            if (dest.getBalance().compareTo(originalTx.getAmount()) < 0) {
                throw new InsufficientFundsException(dest.getAccountNumber(), dest.getBalance(), originalTx.getAmount());
            }

            dest.setBalance(dest.getBalance().subtract(originalTx.getAmount()));
            accountRepository.save(dest);

            // Reversal double-entry: Debit Customer Account, Credit Central Reserve
            ledgerService.recordEntry(originalTx, dest, EntryType.DEBIT, originalTx.getAmount(), dest.getCurrency(),
                    "REVERSAL_DEPOSIT: " + reason.trim());
            ledgerService.recordEntry(originalTx, null, EntryType.CREDIT, originalTx.getAmount(), dest.getCurrency(),
                    "REVERSAL_CENTRAL_RESERVE: " + reason.trim());

        } else if (originalTx.getType() == TransactionType.WITHDRAWAL) {
            Account src = accountRepository.findByAccountNumberWithLock(originalTx.getSourceAccount().getAccountNumber())
                    .orElseThrow(() -> new AccountNotFoundException(originalTx.getSourceAccount().getAccountNumber()));

            src.setBalance(src.getBalance().add(originalTx.getAmount()));
            accountRepository.save(src);

            // Reversal double-entry: Debit Central Reserve, Credit Customer Account
            ledgerService.recordEntry(originalTx, null, EntryType.DEBIT, originalTx.getAmount(), src.getCurrency(),
                    "REVERSAL_CENTRAL_RESERVE: " + reason.trim());
            ledgerService.recordEntry(originalTx, src, EntryType.CREDIT, originalTx.getAmount(), src.getCurrency(),
                    "REVERSAL_WITHDRAWAL: " + reason.trim());

        } else if (originalTx.getType() == TransactionType.TRANSFER) {
            Long srcId = originalTx.getSourceAccount().getId();
            Long dstId = originalTx.getDestinationAccount().getId();
            Long firstId = Math.min(srcId, dstId);
            Long secondId = Math.max(srcId, dstId);

            Account firstLocked = accountRepository.findByIdWithLock(firstId)
                    .orElseThrow(() -> new AccountNotFoundException(firstId));
            Account secondLocked = accountRepository.findByIdWithLock(secondId)
                    .orElseThrow(() -> new AccountNotFoundException(secondId));

            Account src = srcId.equals(firstId) ? firstLocked : secondLocked;
            Account dst = dstId.equals(firstId) ? firstLocked : secondLocked;

            if (dst.getBalance().compareTo(originalTx.getAmount()) < 0) {
                throw new InsufficientFundsException(dst.getAccountNumber(), dst.getBalance(), originalTx.getAmount());
            }

            dst.setBalance(dst.getBalance().subtract(originalTx.getAmount()));
            src.setBalance(src.getBalance().add(originalTx.getAmount()));
            accountRepository.save(dst);
            accountRepository.save(src);

            // Reversal double-entry: Debit Destination Account, Credit Source Account
            ledgerService.recordEntry(originalTx, dst, EntryType.DEBIT, originalTx.getAmount(), dst.getCurrency(),
                    "REVERSAL_TRANSFER_DEBIT: " + dst.getAccountNumber());
            ledgerService.recordEntry(originalTx, src, EntryType.CREDIT, originalTx.getAmount(), src.getCurrency(),
                    "REVERSAL_TRANSFER_CREDIT: " + src.getAccountNumber());
        }

        originalTx.setStatus(TransactionStatus.REVERSED);
        originalTx.setDescription(originalTx.getDescription() + " [REVERSED: " + reason.trim() + "]");
        FinancialTransaction saved = transactionRepository.save(originalTx);

        auditService.logAction(
                principal.getId(),
                "TRANSACTION_REVERSAL",
                "TRANSACTION",
                saved.getId(),
                String.format("Reversed transaction #%d (%s of %s %s). Reason: %s",
                        saved.getId(), saved.getType(), saved.getAmount(), saved.getCurrency(), reason.trim()),
                ipAddress
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long transactionId, UserPrincipal principal) {
        FinancialTransaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));

        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.AUDITOR) {
            boolean isSender = tx.getSourceAccount() != null && tx.getSourceAccount().getOwner().getId().equals(principal.getId());
            boolean isReceiver = tx.getDestinationAccount() != null && tx.getDestinationAccount().getOwner().getId().equals(principal.getId());
            boolean isCreator = tx.getCreatedBy().equals(principal.getId());
            if (!isSender && !isReceiver && !isCreator) {
                throw new UnauthorizedAccessException("You are not authorized to view this transaction.");
            }
        }

        return mapToResponse(tx);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsForUser(UserPrincipal principal, Pageable pageable) {
        if (principal.getRole() == UserRole.ADMIN || principal.getRole() == UserRole.AUDITOR) {
            return transactionRepository.findAll(pageable).map(this::mapToResponse);
        }

        List<Long> userAccountIds = accountRepository.findByOwnerIdOrderByCreatedAtDesc(principal.getId())
                .stream().map(Account::getId).toList();

        if (userAccountIds.isEmpty()) {
            return transactionRepository.findByCreatedByOrderByCreatedAtDesc(principal.getId(), pageable)
                    .map(this::mapToResponse);
        }

        return transactionRepository.findByUserInvolvement(principal.getId(), userAccountIds, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsForAccount(Long accountId, UserPrincipal principal, Pageable pageable) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.AUDITOR) {
            if (!account.getOwner().getId().equals(principal.getId())) {
                throw new UnauthorizedAccessException("You are not authorized to view transactions for this account.");
            }
        }

        return transactionRepository.findByAccountId(accountId, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable).map(this::mapToResponse);
    }

    private TransactionResponse mapToResponse(FinancialTransaction tx) {
        List<LedgerEntryResponse> entries = ledgerService.getEntriesForTransaction(tx.getId());
        return new TransactionResponse(
                tx.getId(),
                tx.getType(),
                tx.getStatus(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getSourceAccount() != null ? tx.getSourceAccount().getAccountNumber() : null,
                tx.getDestinationAccount() != null ? tx.getDestinationAccount().getAccountNumber() : null,
                tx.getDescription(),
                tx.getCreatedBy(),
                tx.getCreatedAt(),
                entries
        );
    }
}

