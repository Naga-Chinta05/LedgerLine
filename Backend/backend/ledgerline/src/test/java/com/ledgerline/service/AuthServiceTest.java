package com.ledgerline.service;

import com.ledgerline.dto.request.ChangePasswordRequest;
import com.ledgerline.dto.request.UpdateProfileRequest;
import com.ledgerline.dto.response.UserProfileResponse;
import com.ledgerline.entity.User;
import com.ledgerline.enums.UserRole;
import com.ledgerline.exception.InvalidTransactionException;
import com.ledgerline.exception.ResourceNotFoundException;
import com.ledgerline.repository.AccountRepository;
import com.ledgerline.repository.UserRepository;
import com.ledgerline.security.JwtTokenProvider;
import com.ledgerline.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("alice@ledgerline.com");
        user.setPasswordHash("encodedOldPassword");
        user.setFullName("Alice Wonder");
        user.setRole(UserRole.ACCOUNT_HOLDER);
        try {
            var f = User.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(user, 12L);
        } catch (Exception ignored) {}
    }

    @Test
    @DisplayName("Update Profile: Modifies full name and phone number with audit logging")
    void testUpdateProfile() {
        when(userRepository.findById(12L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UpdateProfileRequest request = new UpdateProfileRequest("Alice Updated", "+919876543210");
        UserProfileResponse response = authService.updateProfile(12L, request, "127.0.0.1");

        assertNotNull(response);
        assertEquals("Alice Updated", response.getFullName());
        assertEquals("+919876543210", response.getPhoneNumber());
        verify(auditService).logAction(eq(12L), eq("UPDATE_PROFILE"), eq("USER"), eq(12L), anyString(), eq("127.0.0.1"));
    }

    @Test
    @DisplayName("Change Password: Successfully updates hash when current password matches")
    void testChangePasswordSuccess() {
        when(userRepository.findById(12L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("CurrentPass@123", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.matches("NewSecret@456", "encodedOldPassword")).thenReturn(false);
        when(passwordEncoder.encode("NewSecret@456")).thenReturn("encodedNewPassword");

        ChangePasswordRequest request = new ChangePasswordRequest("CurrentPass@123", "NewSecret@456");
        assertDoesNotThrow(() -> authService.changePassword(12L, request, "127.0.0.1"));

        assertEquals("encodedNewPassword", user.getPasswordHash());
        verify(userRepository).save(user);
        verify(auditService).logAction(eq(12L), eq("CHANGE_PASSWORD"), eq("USER"), eq(12L), anyString(), eq("127.0.0.1"));
    }

    @Test
    @DisplayName("Change Password: Rejects when current password is wrong")
    void testChangePasswordWrongCurrent() {
        when(userRepository.findById(12L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass", "encodedOldPassword")).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest("WrongPass", "NewSecret@456");
        assertThrows(InvalidTransactionException.class, () -> authService.changePassword(12L, request, "127.0.0.1"));
        verify(userRepository, never()).save(any());
    }
}

