package com.ledgerline.service;

import com.ledgerline.dto.request.DepositRequest;
import com.ledgerline.dto.request.TransferRequest;
import com.ledgerline.dto.request.WithdrawalRequest;
import com.ledgerline.dto.response.TransactionResponse;
import com.ledgerline.entity.Account;
import com.ledgerline.entity.FinancialTransaction;
import com.ledgerline.entity.User;
import com.ledgerline.enums.AccountStatus;
import com.ledgerline.enums.EntryType;
import com.ledgerline.enums.TransactionStatus;
import com.ledgerline.enums.TransactionType;
import com.ledgerline.enums.UserRole;
import com.ledgerline.exception.AccountInactiveException;
import com.ledgerline.exception.IdempotencyConflictException;
import com.ledgerline.exception.InsufficientFundsException;
import com.ledgerline.exception.InvalidTransactionException;
import com.ledgerline.exception.UnauthorizedAccessException;
import com.ledgerline.repository.AccountRepository;
import com.ledgerline.repository.FinancialTransactionRepository;
import com.ledgerline.security.UserPrincipal;
import com.ledgerline.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private FinancialTransactionRepository transactionRepository;

    @Mock
    private LedgerService ledgerService;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User testUser;
    private UserPrincipal principal;
    private Account sourceAccount;
    private Account destAccount;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("john@example.com");
        testUser.setFullName("John Doe");
        testUser.setRole(UserRole.ACCOUNT_HOLDER);

        // Use reflection or constructor to simulate ID
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(testUser, 1L);
        } catch (Exception ignored) {}

        principal = UserPrincipal.create(testUser);

        sourceAccount = new Account();
        sourceAccount.setAccountNumber("LL-1111");
        sourceAccount.setOwner(testUser);
        sourceAccount.setBalance(new BigDecimal("1000.00"));
        sourceAccount.setCurrency("INR");
        sourceAccount.setStatus(AccountStatus.ACTIVE);

        try {
            var field = Account.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(sourceAccount, 10L);
        } catch (Exception ignored) {}

        destAccount = new Account();
        destAccount.setAccountNumber("LL-2222");
        destAccount.setOwner(testUser);
        destAccount.setBalance(new BigDecimal("500.00"));
        destAccount.setCurrency("INR");
        destAccount.setStatus(AccountStatus.ACTIVE);

        try {
            var field = Account.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(destAccount, 20L);
        } catch (Exception ignored) {}
    }

    @Test
    @DisplayName("Deposit: Increases balance and creates balanced double-entry ledger entries")
    void testDepositSuccess() {
        DepositRequest request = new DepositRequest("LL-1111", new BigDecimal("500.00"), "Test deposit");

        when(idempotencyService.computeHash(any(Object[].class))).thenReturn("hash123");
        when(idempotencyService.getCompletedResponse(anyLong(), any(), any())).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumberWithLock("LL-1111")).thenReturn(Optional.of(sourceAccount));
        when(transactionRepository.save(any(FinancialTransaction.class))).thenAnswer(i -> {
            FinancialTransaction tx = i.getArgument(0);
            try {
                var field = FinancialTransaction.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(tx, 100L);
            } catch (Exception ignored) {}
            return tx;
        });

        TransactionResponse response = transactionService.deposit(request, "idemp-001", principal, "127.0.0.1");

        assertNotNull(response);
        assertEquals(new BigDecimal("1500.00"), sourceAccount.getBalance());
        verify(ledgerService).recordEntry(any(), isNull(), eq(EntryType.DEBIT), eq(new BigDecimal("500.00")), eq("INR"), eq("CENTRAL_CASH_RESERVE"));
        verify(ledgerService).recordEntry(any(), eq(sourceAccount), eq(EntryType.CREDIT), eq(new BigDecimal("500.00")), eq("INR"), contains("LL-1111"));
        verify(auditService).logAction(eq(1L), eq("DEPOSIT"), eq("TRANSACTION"), eq(100L), anyString(), eq("127.0.0.1"));
    }

    @Test
    @DisplayName("Withdrawal: Deducts balance and checks sufficient funds")
    void testWithdrawalSuccess() {
        WithdrawalRequest request = new WithdrawalRequest("LL-1111", new BigDecimal("400.00"), "ATM withdrawal");

        when(idempotencyService.computeHash(any(Object[].class))).thenReturn("hash123");
        when(idempotencyService.getCompletedResponse(anyLong(), any(), any())).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumberWithLock("LL-1111")).thenReturn(Optional.of(sourceAccount));
        when(transactionRepository.save(any(FinancialTransaction.class))).thenAnswer(i -> {
            FinancialTransaction tx = i.getArgument(0);
            try {
                var field = FinancialTransaction.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(tx, 101L);
            } catch (Exception ignored) {}
            return tx;
        });

        TransactionResponse response = transactionService.withdraw(request, "idemp-002", principal, "127.0.0.1");

        assertNotNull(response);
        assertEquals(new BigDecimal("600.00"), sourceAccount.getBalance());
        verify(ledgerService).recordEntry(any(), eq(sourceAccount), eq(EntryType.DEBIT), eq(new BigDecimal("400.00")), eq("INR"), contains("LL-1111"));
        verify(ledgerService).recordEntry(any(), isNull(), eq(EntryType.CREDIT), eq(new BigDecimal("400.00")), eq("INR"), eq("ATM_CASH_DISBURSEMENT"));
    }

    @Test
    @DisplayName("Withdrawal: Rejects with InsufficientFundsException when balance is too low")
    void testWithdrawalInsufficientFunds() {
        WithdrawalRequest request = new WithdrawalRequest("LL-1111", new BigDecimal("1500.00"), "Overdraft attempt");

        when(idempotencyService.computeHash(any(Object[].class))).thenReturn("hash123");
        when(idempotencyService.getCompletedResponse(anyLong(), any(), any())).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumberWithLock("LL-1111")).thenReturn(Optional.of(sourceAccount));

        assertThrows(InsufficientFundsException.class, () ->
                transactionService.withdraw(request, "idemp-003", principal, "127.0.0.1")
        );

        // Verify balance was untouched
        assertEquals(new BigDecimal("1000.00"), sourceAccount.getBalance());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Transfer: Atomically moves funds between accounts with lock ordering")
    void testTransferSuccess() {
        TransferRequest request = new TransferRequest("LL-1111", "LL-2222", new BigDecimal("300.00"), "Rent payment");

        when(idempotencyService.computeHash(any(Object[].class))).thenReturn("hash123");
        when(idempotencyService.getCompletedResponse(anyLong(), any(), any())).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumber("LL-1111")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber("LL-2222")).thenReturn(Optional.of(destAccount));
        when(accountRepository.findByIdWithLock(10L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByIdWithLock(20L)).thenReturn(Optional.of(destAccount));
        when(transactionRepository.save(any(FinancialTransaction.class))).thenAnswer(i -> {
            FinancialTransaction tx = i.getArgument(0);
            try {
                var field = FinancialTransaction.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(tx, 102L);
            } catch (Exception ignored) {}
            return tx;
        });

        TransactionResponse response = transactionService.transfer(request, "idemp-004", principal, "127.0.0.1");

        assertNotNull(response);
        assertEquals(new BigDecimal("700.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("800.00"), destAccount.getBalance());
        verify(ledgerService).recordEntry(any(), eq(sourceAccount), eq(EntryType.DEBIT), eq(new BigDecimal("300.00")), eq("INR"), contains("TRANSFER_OUT"));
        verify(ledgerService).recordEntry(any(), eq(destAccount), eq(EntryType.CREDIT), eq(new BigDecimal("300.00")), eq("INR"), contains("TRANSFER_IN"));
    }

    @Test
    @DisplayName("Transfer: Rejects self-transfer to same account")
    void testSelfTransferRejection() {
        TransferRequest request = new TransferRequest("LL-1111", "LL-1111", new BigDecimal("100.00"), "Self transfer");

        assertThrows(InvalidTransactionException.class, () ->
                transactionService.transfer(request, "idemp-005", principal, "127.0.0.1")
        );
    }

    @Test
    @DisplayName("Transfer: Rejects inactive destination account")
    void testTransferToInactiveAccount() {
        destAccount.setStatus(AccountStatus.FROZEN);
        TransferRequest request = new TransferRequest("LL-1111", "LL-2222", new BigDecimal("100.00"), "Transfer to frozen");

        when(idempotencyService.computeHash(any(Object[].class))).thenReturn("hash123");
        when(idempotencyService.getCompletedResponse(anyLong(), any(), any())).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumber("LL-1111")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber("LL-2222")).thenReturn(Optional.of(destAccount));
        when(accountRepository.findByIdWithLock(10L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByIdWithLock(20L)).thenReturn(Optional.of(destAccount));

        assertThrows(AccountInactiveException.class, () ->
                transactionService.transfer(request, "idemp-006", principal, "127.0.0.1")
        );
    }

    @Test
    @DisplayName("Idempotency: Returns cached response on duplicate request")
    void testIdempotencyReplay() {
        DepositRequest request = new DepositRequest("LL-1111", new BigDecimal("100.00"), "Duplicate request");
        TransactionResponse cachedResponse = new TransactionResponse();
        cachedResponse.setId(999L);
        cachedResponse.setAmount(new BigDecimal("100.00"));

        when(idempotencyService.computeHash(any(Object[].class))).thenReturn("hash999");
        when(idempotencyService.getCompletedResponse(eq(1L), eq("repeat-key"), eq("hash999")))
                .thenReturn(Optional.of(cachedResponse));

        TransactionResponse result = transactionService.deposit(request, "repeat-key", principal, "127.0.0.1");

        assertEquals(999L, result.getId());
        verify(accountRepository, never()).findByAccountNumberWithLock(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Reversal: Successfully reverses POSTED transfer and creates offsetting double-entry ledger lines")
    void testReverseTransferSuccess() {
        FinancialTransaction tx = new FinancialTransaction();
        tx.setType(TransactionType.TRANSFER);
        tx.setStatus(TransactionStatus.POSTED);
        tx.setAmount(new BigDecimal("300.00"));
        tx.setCurrency("INR");
        tx.setSourceAccount(sourceAccount);
        tx.setDestinationAccount(destAccount);
        tx.setDescription("Original Transfer");
        tx.setCreatedBy(1L);
        try {
            var f = FinancialTransaction.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(tx, 77L);
        } catch (Exception ignored) {}

        when(transactionRepository.findById(77L)).thenReturn(Optional.of(tx));
        when(accountRepository.findByIdWithLock(10L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByIdWithLock(20L)).thenReturn(Optional.of(destAccount));
        when(transactionRepository.save(any(FinancialTransaction.class))).thenAnswer(i -> i.getArgument(0));

        User adminUser = new User();
        adminUser.setEmail("admin@ledgerline.com");
        adminUser.setFullName("System Admin");
        adminUser.setRole(UserRole.ADMIN);
        try {
            var f = User.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(adminUser, 99L);
        } catch (Exception ignored) {}
        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);

        BigDecimal srcBefore = sourceAccount.getBalance();
        BigDecimal dstBefore = destAccount.getBalance();

        TransactionResponse response = transactionService.reverseTransaction(77L, "Dispute resolved", adminPrincipal, "127.0.0.1");

        assertNotNull(response);
        assertEquals(TransactionStatus.REVERSED, response.getStatus());
        // Source refunded +300
        assertEquals(srcBefore.add(new BigDecimal("300.00")), sourceAccount.getBalance());
        // Dest deducted -300
        assertEquals(dstBefore.subtract(new BigDecimal("300.00")), destAccount.getBalance());

        // Verified double-entry reversal
        verify(ledgerService).recordEntry(eq(tx), eq(destAccount), eq(EntryType.DEBIT), eq(new BigDecimal("300.00")), eq("INR"), contains("REVERSAL_TRANSFER_DEBIT"));
        verify(ledgerService).recordEntry(eq(tx), eq(sourceAccount), eq(EntryType.CREDIT), eq(new BigDecimal("300.00")), eq("INR"), contains("REVERSAL_TRANSFER_CREDIT"));
        verify(auditService).logAction(eq(99L), eq("TRANSACTION_REVERSAL"), eq("TRANSACTION"), eq(77L), anyString(), eq("127.0.0.1"));
    }

    @Test
    @DisplayName("Reversal: Rejects reversal when caller is not Admin or Auditor")
    void testReverseUnauthorized() {
        assertThrows(UnauthorizedAccessException.class, () ->
                transactionService.reverseTransaction(77L, "Invalid reason", principal, "127.0.0.1")
        );
    }
}

