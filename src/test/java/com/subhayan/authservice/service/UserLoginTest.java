package com.subhayan.authservice.service;

import com.subhayan.authservice.dto.DtoAuthLogin.AuthResponse;
import com.subhayan.authservice.dto.DtoAuthLogin.LoginRequest;
import com.subhayan.authservice.entity.Role;
import com.subhayan.authservice.entity.Salutation;
import com.subhayan.authservice.entity.UserEntity;
import com.subhayan.authservice.exception.InvalidCredentialsException;
import com.subhayan.authservice.repository.UserRepository;
import com.subhayan.authservice.security.JwtUtil;
import com.subhayan.authservice.security.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserLoginTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserLogin userLogin;

    private UserEntity userEntity;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userEntity = UserEntity.builder()
                .id(userId)
                .email("john@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .salutation(Salutation.MR)
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .active(true)
                .build();
    }

    @Test
    void loginUser_withValidCredentials_returnsAuthResponse() {
        LoginRequest request = new LoginRequest("john@example.com", "password123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(userId.toString())).thenReturn("jwt-access-token");
        when(refreshTokenService.generateRefreshToken(userId.toString())).thenReturn("refresh-token-uuid");

        AuthResponse response = userLogin.loginUser(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token-uuid");
    }

    @Test
    void loginUser_withNonExistentEmail_throwsInvalidCredentialsException() {
        LoginRequest request = new LoginRequest("unknown@example.com", "password123");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userLogin.loginUser(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void loginUser_withWrongPassword_throwsInvalidCredentialsException() {
        LoginRequest request = new LoginRequest("john@example.com", "wrongpassword");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> userLogin.loginUser(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(jwtUtil, never()).generateToken(anyString());
        verify(refreshTokenService, never()).generateRefreshToken(anyString());
    }
}
