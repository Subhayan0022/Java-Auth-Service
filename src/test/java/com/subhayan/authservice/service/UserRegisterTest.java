package com.subhayan.authservice.service;

import com.subhayan.authservice.dto.DtoAuthRegister.UserRegisterRequestDTO;
import com.subhayan.authservice.dto.DtoAuthRegister.UserRegisterResponseDTO;
import com.subhayan.authservice.entity.Role;
import com.subhayan.authservice.entity.Salutation;
import com.subhayan.authservice.entity.UserEntity;
import com.subhayan.authservice.exception.UserAlreadyExistsException;
import com.subhayan.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRegisterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserRegister userRegister;

    private UserRegisterRequestDTO validRequest;
    private UserEntity savedEntity;

    @BeforeEach
    void setUp() {
        validRequest = new UserRegisterRequestDTO(
                Salutation.MR, "John", "Doe", "john@example.com",
                "password123", "+1234567890", LocalDate.now().minusYears(25)
        );

        savedEntity = UserEntity.builder()
                .id(UUID.randomUUID())
                .salutation(Salutation.MR)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .phoneNumber("+1234567890")
                .dateOfBirth(LocalDate.now().minusYears(25))
                .role(Role.USER)
                .active(true)
                .build();
    }

    @Test
    void registerUser_withValidData_returnsResponseDTO() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        UserRegisterResponseDTO response = userRegister.registerUser(validRequest);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(savedEntity.getId());
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.salutation()).isEqualTo(Salutation.MR);
        assertThat(response.role()).isEqualTo(Role.USER);
    }

    @Test
    void registerUser_withDuplicateEmail_throwsUserAlreadyExistsException() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userRegister.registerUser(validRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("A user with this email already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_withAgeTooYoung_throwsIllegalArgumentException() {
        UserRegisterRequestDTO youngUser = new UserRegisterRequestDTO(
                Salutation.MR, "Kid", "User", "kid@example.com",
                "password123", "+1234567890", LocalDate.now().minusYears(12)
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertThatThrownBy(() -> userRegister.registerUser(youngUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User must be between 13 and 100 years old");

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_withAgeTooOld_throwsIllegalArgumentException() {
        UserRegisterRequestDTO oldUser = new UserRegisterRequestDTO(
                Salutation.MRS, "Old", "User", "old@example.com",
                "password123", "+1234567890", LocalDate.now().minusYears(101)
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertThatThrownBy(() -> userRegister.registerUser(oldUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User must be between 13 and 100 years old");
    }
}
