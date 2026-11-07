package com.ledgerline.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledgerline.dto.request.LoginRequest;
import com.ledgerline.dto.request.RegisterRequest;
import com.ledgerline.dto.response.AuthResponse;
import com.ledgerline.enums.UserRole;
import com.ledgerline.security.JwtAuthenticationEntryPoint;
import com.ledgerline.security.JwtAuthenticationFilter;
import com.ledgerline.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("POST /api/auth/register: Registers new user and returns JWT token")
    void testRegister() throws Exception {
        RegisterRequest request = new RegisterRequest("alice@example.com", "Password@123", "Alice Cooper", UserRole.ACCOUNT_HOLDER);
        AuthResponse response = new AuthResponse("mock-jwt-token", 10L, "alice@example.com", "Alice Cooper", UserRole.ACCOUNT_HOLDER);

        when(authService.register(any(RegisterRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.role").value("ACCOUNT_HOLDER"));
    }

    @Test
    @DisplayName("POST /api/auth/login: Authenticates user and returns JWT token")
    void testLogin() throws Exception {
        LoginRequest request = new LoginRequest("alice@example.com", "Password@123");
        AuthResponse response = new AuthResponse("mock-jwt-token", 10L, "alice@example.com", "Alice Cooper", UserRole.ACCOUNT_HOLDER);

        when(authService.login(any(LoginRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }
}

