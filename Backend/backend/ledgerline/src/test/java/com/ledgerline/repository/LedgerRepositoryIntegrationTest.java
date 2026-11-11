package com.ledgerline.repository;

import com.ledgerline.entity.*;
import com.ledgerline.enums.AccountStatus;
import com.ledgerline.enums.EntryType;
import com.ledgerline.enums.TransactionStatus;
import com.ledgerline.enums.TransactionType;
import com.ledgerline.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class LedgerRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private FinancialTransactionRepository transactionRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private IdempotencyRecordRepository idempotencyRecordRepository;

    private User testUser;
    private Account testAccount1;
    private Account testAccount2;

    @BeforeEach
    void setUp() {
        testUser = new User("data.test@ledgerline.com", "$2a$10$hashedpassword", "Data Test User", UserRole.ACCOUNT_HOLDER);
        testUser = userRepository.save(testUser);

        testAccount1 = new Account("ACT-DATA-001", testUser, new BigDecimal("5000.00"), "INR", AccountStatus.ACTIVE);
        testAccount1 = accountRepository.save(testAccount1);

        testAccount2 = new Account("ACT-DATA-002", testUser, new BigDecimal("2000.00"), "INR", AccountStatus.ACTIVE);
        testAccount2 = accountRepository.save(testAccount2);
    }

    @Test
    @DisplayName("Verify Account pessimistic locking and liquidity aggregates")
    void testAccountRepositoryOperations() {
        Optional<Account> lockedOpt = accountRepository.findByIdWithLock(testAccount1.getId());
        assertTrue(lockedOpt.isPresent());
        assertEquals("ACT-DATA-001", lockedOpt.get().getAccountNumber());

        Optional<Account> lockedByNum = accountRepository.findByAccountNumberWithLock("ACT-DATA-002");
        assertTrue(lockedByNum.isPresent());
        assertEquals(testAccount2.getId(), lockedByNum.get().getId());

        List<Account> lockedMulti = accountRepository.findAllByIdWithLock(List.of(testAccount1.getId(), testAccount2.getId()));
        assertEquals(2, lockedMulti.size());

        BigDecimal totalLiquidity = accountRepository.sumTotalLiquidity();
        assertTrue(totalLiquidity.compareTo(new BigDecimal("7000.00")) >= 0);

        long activeCount = accountRepository.countByStatus(AccountStatus.ACTIVE);
        assertTrue(activeCount >= 2);
    }

    @Test
    @DisplayName("Verify Transaction queries and User involvement indexing")
    void testFinancialTransactionRepository() {
        FinancialTransaction tx = new FinancialTransaction(
                TransactionType.TRANSFER,
                TransactionStatus.POSTED,
                new BigDecimal("1500.00"),
                "INR",
                testAccount1,
                testAccount2,
                "Intra-account funds transfer",
                testUser.getId()
        );
        tx = transactionRepository.save(tx);

        Page<FinancialTransaction> byAccount = transactionRepository.findByAccountId(testAccount1.getId(), PageRequest.of(0, 10));
        assertFalse(byAccount.isEmpty());
        assertEquals(tx.getId(), byAccount.getContent().get(0).getId());

        Page<FinancialTransaction> byInvolvement = transactionRepository.findByUserInvolvement(
                testUser.getId(),
                List.of(testAccount1.getId(), testAccount2.getId()),
                PageRequest.of(0, 10)
        );
        assertFalse(byInvolvement.isEmpty());

        BigDecimal totalVolume = transactionRepository.sumTotalVolume();
        assertTrue(totalVolume.compareTo(new BigDecimal("1500.00")) >= 0);

        Instant start = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant end = Instant.now().plus(1, ChronoUnit.DAYS);
        List<FinancialTransaction> rangeList = transactionRepository.findByAccountIdAndDateRange(testAccount1.getId(), start, end);
        assertFalse(rangeList.isEmpty());
    }

    @Test
    @DisplayName("Verify Double-Entry Ledger queries and balance integrity")
    void testLedgerEntryCalculations() {
        FinancialTransaction tx = new FinancialTransaction(
                TransactionType.TRANSFER,
                TransactionStatus.POSTED,
                new BigDecimal("1000.00"),
                "INR",
                testAccount1,
                testAccount2,
                "Ledger test transfer",
                testUser.getId()
        );
        tx = transactionRepository.save(tx);

        LedgerEntry debitEntry = new LedgerEntry(tx, testAccount1, EntryType.DEBIT, new BigDecimal("1000.00"), "INR", "Account Debit");
        LedgerEntry creditEntry = new LedgerEntry(tx, testAccount2, EntryType.CREDIT, new BigDecimal("1000.00"), "INR", "Account Credit");
        ledgerEntryRepository.save(debitEntry);
        ledgerEntryRepository.save(creditEntry);

        List<LedgerEntry> entries = ledgerEntryRepository.findByTransactionIdOrderByIdAsc(tx.getId());
        assertEquals(2, entries.size());

        BigDecimal sumDebits = ledgerEntryRepository.sumAmountByAccountAndEntryType(testAccount1.getId(), EntryType.DEBIT);
        BigDecimal sumCredits = ledgerEntryRepository.sumAmountByAccountAndEntryType(testAccount2.getId(), EntryType.CREDIT);
        assertEquals(0, sumDebits.compareTo(new BigDecimal("1000.00")));
        assertEquals(0, sumCredits.compareTo(new BigDecimal("1000.00")));

        Instant start = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant end = Instant.now().plus(1, ChronoUnit.HOURS);
        List<LedgerEntry> statementEntries = ledgerEntryRepository.findByAccountIdAndDateRange(testAccount1.getId(), start, end);
        assertFalse(statementEntries.isEmpty());
    }

    @Test
    @DisplayName("Verify AuditLog and Idempotency persistence")
    void testAuditAndIdempotencyPersistence() {
        AuditLog audit = new AuditLog(testUser.getId(), "ACCOUNT_CREATION", "ACCOUNT", testAccount1.getId(), "Account created successfully", "127.0.0.1");
        audit = auditLogRepository.save(audit);

        Page<AuditLog> auditPage = auditLogRepository.findByActorUserIdOrderByTimestampDesc(testUser.getId(), PageRequest.of(0, 10));
        assertFalse(auditPage.isEmpty());
        assertEquals(audit.getId(), auditPage.getContent().get(0).getId());

        IdempotencyRecord record = new IdempotencyRecord(testUser.getId(), "IDEM-TEST-1001", "hashabc123", null, "PROCESSED", "{\"status\":\"SUCCESS\"}");
        idempotencyRecordRepository.save(record);

        assertTrue(idempotencyRecordRepository.existsByUserIdAndIdempotencyKey(testUser.getId(), "IDEM-TEST-1001"));
        Optional<IdempotencyRecord> retrieved = idempotencyRecordRepository.findByUserIdAndIdempotencyKey(testUser.getId(), "IDEM-TEST-1001");
        assertTrue(retrieved.isPresent());
        assertEquals("PROCESSED", retrieved.get().getStatus());
    }
}

