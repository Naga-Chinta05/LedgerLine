package com.ledgerline.config;

import com.ledgerline.dto.request.DepositRequest;
import com.ledgerline.dto.request.TransferRequest;
import com.ledgerline.entity.Account;
import com.ledgerline.entity.User;
import com.ledgerline.enums.AccountStatus;
import com.ledgerline.enums.UserRole;
import com.ledgerline.repository.AccountRepository;
import com.ledgerline.repository.UserRepository;
import com.ledgerline.security.UserPrincipal;
import com.ledgerline.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionService transactionService;

    public DataInitializer(UserRepository userRepository,
                           AccountRepository accountRepository,
                           PasswordEncoder passwordEncoder,
                           TransactionService transactionService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.transactionService = transactionService;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already initialized, skipping seed data.");
            return;
        }

        log.info("Seeding initial users and banking accounts for LedgerLine demonstration...");

        // 1. Admin User
        User admin = new User();
        admin.setEmail("admin@ledgerline.com");
        admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
        admin.setFullName("System Administrator");
        admin.setRole(UserRole.ADMIN);
        admin.setEnabled(true);
        User savedAdmin = userRepository.save(admin);

        // 2. Auditor User
        User auditor = new User();
        auditor.setEmail("auditor@ledgerline.com");
        auditor.setPasswordHash(passwordEncoder.encode("Auditor@123"));
        auditor.setFullName("Lead Compliance Auditor");
        auditor.setRole(UserRole.AUDITOR);
        auditor.setEnabled(true);
        userRepository.save(auditor);

        // 3. Customer 1: John Doe
        User john = new User();
        john.setEmail("john.doe@example.com");
        john.setPasswordHash(passwordEncoder.encode("User@123"));
        john.setFullName("John Doe");
        john.setRole(UserRole.ACCOUNT_HOLDER);
        john.setEnabled(true);
        User savedJohn = userRepository.save(john);

        Account johnAccount = new Account();
        johnAccount.setAccountNumber("LL-1001001001");
        johnAccount.setOwner(savedJohn);
        johnAccount.setBalance(BigDecimal.ZERO);
        johnAccount.setCurrency("INR");
        johnAccount.setStatus(AccountStatus.ACTIVE);
        Account savedJohnAccount = accountRepository.save(johnAccount);

        // 4. Customer 2: Jane Smith
        User jane = new User();
        jane.setEmail("jane.smith@example.com");
        jane.setPasswordHash(passwordEncoder.encode("User@123"));
        jane.setFullName("Jane Smith");
        jane.setRole(UserRole.ACCOUNT_HOLDER);
        jane.setEnabled(true);
        User savedJane = userRepository.save(jane);

        Account janeAccount = new Account();
        janeAccount.setAccountNumber("LL-2002002002");
        janeAccount.setOwner(savedJane);
        janeAccount.setBalance(BigDecimal.ZERO);
        janeAccount.setCurrency("INR");
        janeAccount.setStatus(AccountStatus.ACTIVE);
        Account savedJaneAccount = accountRepository.save(janeAccount);

        // Seed initial transactions via TransactionService to create proper balanced double-entry ledger entries!
        UserPrincipal adminPrincipal = UserPrincipal.create(savedAdmin);

        // Deposit 50,000 INR into John's account
        transactionService.deposit(
                new DepositRequest("LL-1001001001", new BigDecimal("50000.00"), "Initial Account Opening Deposit"),
                "seed-deposit-john-001",
                adminPrincipal,
                "127.0.0.1"
        );

        // Deposit 25,000 INR into Jane's account
        transactionService.deposit(
                new DepositRequest("LL-2002002002", new BigDecimal("25000.00"), "Initial Account Opening Deposit"),
                "seed-deposit-jane-002",
                adminPrincipal,
                "127.0.0.1"
        );

        // Transfer 5,000 INR from John to Jane
        UserPrincipal johnPrincipal = UserPrincipal.create(savedJohn);
        transactionService.transfer(
                new TransferRequest("LL-1001001001", "LL-2002002002", new BigDecimal("5000.00"), "Consulting Services Payment"),
                "seed-transfer-john-jane-003",
                johnPrincipal,
                "127.0.0.1"
        );

        log.info("LedgerLine demo seed data created successfully:");
        log.info("  Admin: admin@ledgerline.com / Admin@123");
        log.info("  Auditor: auditor@ledgerline.com / Auditor@123");
        log.info("  Customer 1: john.doe@example.com / User@123 (Account: LL-1001001001, Balance: 45000.00 INR)");
        log.info("  Customer 2: jane.smith@example.com / User@123 (Account: LL-2002002002, Balance: 30000.00 INR)");
    }
}
