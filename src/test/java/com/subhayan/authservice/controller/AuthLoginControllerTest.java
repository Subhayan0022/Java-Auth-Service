package com.subhayan.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subhayan.authservice.dto.DtoAuthLogin.AuthResponse;
import com.subhayan.authservice.dto.DtoAuthLogin.LoginRequest;
import com.subhayan.authservice.exception.InvalidCredentialsException;
import com.subhayan.authservice.security.JwtAuthFilter;
import com.subhayan.authservice.security.RateLimiterFilter;
import com.subhayan.authservice.service.UserLogin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AuthLogin.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {RateLimiterFilter.class, JwtAuthFilter.class})
})
@Import(TestSecurityConfig.class)
class AuthLoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserLogin userLogin;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser
    void loginUser_withValidCredentials_returns200WithTokens() throws Exception {
        LoginRequest request = new LoginRequest("john@example.com", "password123");
        AuthResponse authResponse = new AuthResponse("jwt-access-token", "refresh-token-uuid");

        when(userLogin.loginUser(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-uuid"));
    }

    @Test
    @WithMockUser
    void loginUser_withInvalidCredentials_returns401() throws Exception {
        LoginRequest request = new LoginRequest("john@example.com", "wrongpassword");

        when(userLogin.loginUser(any())).thenThrow(
                new InvalidCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @WithMockUser
    void loginUser_withBlankEmail_returns400() throws Exception {
        String invalidJson = """
                {
                    "email": "",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
