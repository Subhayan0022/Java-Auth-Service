package com.subhayan.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subhayan.authservice.dto.DtoAuthLogin.RefreshRequest;
import com.subhayan.authservice.security.CustomUserDetailsService;
import com.subhayan.authservice.security.JwtAuthFilter;
import com.subhayan.authservice.security.JwtUtil;
import com.subhayan.authservice.security.RateLimiterFilter;
import com.subhayan.authservice.security.RefreshTokenService;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AuthTokenController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {RateLimiterFilter.class, JwtAuthFilter.class})
})
@Import(TestSecurityConfig.class)
class AuthTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser
    void refresh_withValidRefreshToken_returnsNewTokens() throws Exception {
        RefreshRequest request = new RefreshRequest("valid-refresh-token");

        when(refreshTokenService.getRefreshToken("valid-refresh-token")).thenReturn("user-id-123");
        when(jwtUtil.generateToken("user-id-123")).thenReturn("new-access-token");
        when(refreshTokenService.generateRefreshToken("user-id-123")).thenReturn("new-refresh-token");

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    @WithMockUser
    void refresh_withInvalidRefreshToken_returns401() throws Exception {
        RefreshRequest request = new RefreshRequest("invalid-token");

        when(refreshTokenService.getRefreshToken("invalid-token")).thenReturn(null);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired refresh token"));
    }

    @Test
    @WithMockUser
    void logout_withValidToken_returns204() throws Exception {
        RefreshRequest request = new RefreshRequest("refresh-token-to-delete");

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(refreshTokenService).deleteRefreshToken("refresh-token-to-delete");
    }

    @Test
    void logout_withoutAuthentication_returns401() throws Exception {
        RefreshRequest request = new RefreshRequest("some-token");

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
