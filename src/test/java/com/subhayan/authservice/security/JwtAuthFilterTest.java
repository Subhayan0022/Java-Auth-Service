package com.subhayan.authservice.security;

import com.subhayan.authservice.entity.Role;
import com.subhayan.authservice.entity.Salutation;
import com.subhayan.authservice.entity.UserEntity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_withNoAuthorizationHeader_continuesChainWithoutAuthentication() throws ServletException, IOException {
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_withValidBearerToken_setsSecurityContext() throws ServletException, IOException {
        String userId = UUID.randomUUID().toString();
        String token = "valid-jwt-token";
        request.addHeader("Authorization", "Bearer " + token);

        UserEntity user = UserEntity.builder()
                .id(UUID.fromString(userId))
                .email("user@example.com")
                .password("encoded")
                .role(Role.USER)
                .salutation(Salutation.MR)
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .active(true)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);

        when(jwtUtil.getUserIDFromToken(token)).thenReturn(userId);
        when(userDetailsService.loadUserById(userId)).thenReturn(userDetails);
        when(jwtUtil.validateToken(token, userId)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("user@example.com");
    }

    @Test
    void doFilter_withInvalidToken_continuesChainWithoutAuthentication() throws ServletException, IOException {
        String userId = UUID.randomUUID().toString();
        String token = "invalid-jwt-token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtil.getUserIDFromToken(token)).thenReturn(userId);
        when(userDetailsService.loadUserById(userId)).thenReturn(
                new CustomUserDetails(UserEntity.builder()
                        .id(UUID.fromString(userId))
                        .email("user@example.com")
                        .password("encoded")
                        .role(Role.USER)
                        .salutation(Salutation.MR)
                        .firstName("Test")
                        .lastName("User")
                        .phoneNumber("+1234567890")
                        .dateOfBirth(LocalDate.of(1990, 1, 1))
                        .active(true)
                        .build())
        );
        when(jwtUtil.validateToken(token, userId)).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_doesNotReauthenticateIfAlreadyAuthenticated() throws ServletException, IOException {
        String userId = UUID.randomUUID().toString();
        String token = "jwt-token";
        request.addHeader("Authorization", "Bearer " + token);

        UserEntity user = UserEntity.builder()
                .id(UUID.fromString(userId))
                .email("existing@example.com")
                .password("encoded")
                .role(Role.USER)
                .salutation(Salutation.MR)
                .firstName("Existing")
                .lastName("User")
                .phoneNumber("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .active(true)
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(user);

        var existingAuth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(jwtUtil.getUserIDFromToken(token)).thenReturn(userId);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(userDetailsService, never()).loadUserById(anyString());
        verify(filterChain).doFilter(request, response);
    }
}
