package com.ledgerline.service;

import com.ledgerline.dto.request.CreateAccountRequest;
import com.ledgerline.dto.response.AccountResponse;
import com.ledgerline.entity.Account;
import com.ledgerline.entity.User;
import com.ledgerline.enums.AccountStatus;
import com.ledgerline.enums.UserRole;
import com.ledgerline.exception.AccountNotFoundException;
import com.ledgerline.repository.AccountRepository;
import com.ledgerline.repository.UserRepository;
import com.ledgerline.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AccountServiceImpl accountService;

    private User user;
    private Account account;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("user@example.com");
        user.setFullName("Test User");
        user.setRole(UserRole.ACCOUNT_HOLDER);
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, 5L);
        } catch (Exception ignored) {}

        account = new Account();
        account.setAccountNumber("LL-5555");
        account.setOwner(user);
        account.setBalance(new BigDecimal("2500.00"));
        account.setCurrency("INR");
        account.setStatus(AccountStatus.ACTIVE);
        try {
            var field = Account.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(account, 50L);
        } catch (Exception ignored) {}
    }

    @Test
    @DisplayName("Create Account: Successfully initializes new bank account with active status")
    void testCreateAccount() {
        CreateAccountRequest request = new CreateAccountRequest("INR", BigDecimal.ZERO);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> {
            Account acc = i.getArgument(0);
            try {
                var field = Account.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(acc, 51L);
            } catch (Exception ignored) {}
            return acc;
        });

        AccountResponse response = accountService.createAccount(5L, request, "127.0.0.1");

        assertNotNull(response);
        assertEquals(51L, response.getId());
        assertEquals("INR", response.getCurrency());
        assertEquals(AccountStatus.ACTIVE, response.getStatus());
        verify(auditService).logAction(eq(5L), eq("CREATE_ACCOUNT"), eq("ACCOUNT"), eq(51L), anyString(), eq("127.0.0.1"));
    }

    @Test
    @DisplayName("Update Account Status: Changes status to FROZEN or CLOSED")
    void testUpdateAccountStatus() {
        when(accountRepository.findById(50L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        AccountResponse response = accountService.updateAccountStatus(50L, AccountStatus.FROZEN, "Suspicious activity review", 1L, "127.0.0.1");

        assertEquals(AccountStatus.FROZEN, response.getStatus());
        verify(auditService).logAction(eq(1L), eq("UPDATE_ACCOUNT_STATUS"), eq("ACCOUNT"), eq(50L), contains("FROZEN"), eq("127.0.0.1"));
    }

    @Test
    @DisplayName("Get Account: Throws AccountNotFoundException for non-existent account")
    void testAccountNotFound() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountById(999L));
    }
}

