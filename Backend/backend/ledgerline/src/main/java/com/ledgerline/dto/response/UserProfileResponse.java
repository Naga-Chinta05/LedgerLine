package com.ledgerline.dto.response;

import com.ledgerline.enums.UserRole;
import java.time.Instant;

public class UserProfileResponse {
    private Long id;
    private String email;
    private String fullName;
    private UserRole role;
    private boolean enabled;
    private Instant createdAt;

    public UserProfileResponse() {}

    public UserProfileResponse(Long id, String email, String fullName, UserRole role, boolean enabled, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.enabled = enabled;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

