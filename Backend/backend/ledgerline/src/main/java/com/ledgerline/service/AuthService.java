package com.ledgerline.service;

import com.ledgerline.dto.request.ChangePasswordRequest;
import com.ledgerline.dto.request.LoginRequest;
import com.ledgerline.dto.request.RegisterRequest;
import com.ledgerline.dto.request.UpdateProfileRequest;
import com.ledgerline.dto.response.AuthResponse;
import com.ledgerline.dto.response.UserProfileResponse;
import com.ledgerline.security.UserPrincipal;

public interface AuthService {
    AuthResponse register(RegisterRequest request, String ipAddress);
    AuthResponse login(LoginRequest request, String ipAddress);
    UserProfileResponse getCurrentUserProfile(UserPrincipal principal);
    UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request, String ipAddress);
    void changePassword(Long userId, ChangePasswordRequest request, String ipAddress);
}
