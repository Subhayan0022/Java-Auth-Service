package com.subhayan.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.subhayan.authservice.dto.DtoAuthRegister.UserRegisterRequestDTO;
import com.subhayan.authservice.dto.DtoAuthRegister.UserRegisterResponseDTO;
import com.subhayan.authservice.entity.Role;
import com.subhayan.authservice.entity.Salutation;
import com.subhayan.authservice.exception.UserAlreadyExistsException;
import com.subhayan.authservice.security.JwtAuthFilter;
import com.subhayan.authservice.security.RateLimiterFilter;
import com.subhayan.authservice.service.UserRegister;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AuthRegister.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {RateLimiterFilter.class, JwtAuthFilter.class})
})
@Import(TestSecurityConfig.class)
class AuthRegisterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRegister userRegister;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @WithMockUser
    void registerUser_withValidRequest_returns201() throws Exception {
        UserRegisterRequestDTO request = new UserRegisterRequestDTO(
                Salutation.MR, "John", "Doe", "john@example.com",
                "password123", "+1234567890", LocalDate.of(1990, 1, 1)
        );

        UserRegisterResponseDTO responseDTO = new UserRegisterResponseDTO(
                UUID.randomUUID(), Salutation.MR, "John", "Doe",
                "john@example.com", "+1234567890", LocalDate.of(1990, 1, 1), Role.USER
        );

        when(userRegister.registerUser(any(UserRegisterRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @WithMockUser
    void registerUser_withDuplicateEmail_returns409() throws Exception {
        UserRegisterRequestDTO request = new UserRegisterRequestDTO(
                Salutation.MR, "John", "Doe", "existing@example.com",
                "password123", "+1234567890", LocalDate.of(1990, 1, 1)
        );

        when(userRegister.registerUser(any())).thenThrow(
                new UserAlreadyExistsException("A user with this email already exists"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A user with this email already exists"));
    }

    @Test
    @WithMockUser
    void registerUser_withInvalidAge_returns400() throws Exception {
        UserRegisterRequestDTO request = new UserRegisterRequestDTO(
                Salutation.MR, "Kid", "User", "kid@example.com",
                "password123", "+1234567890", LocalDate.now().minusYears(10)
        );

        when(userRegister.registerUser(any())).thenThrow(
                new IllegalArgumentException("INVALID AGE : User must be between 13 and 100 years old"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void registerUser_withBlankEmail_returns400() throws Exception {
        String invalidJson = """
                {
                    "salutation": "MR",
                    "firstName": "John",
                    "lastName": "Doe",
                    "email": "",
                    "password": "password123",
                    "phoneNumber": "+1234567890",
                    "dateOfBirth": "1990-01-01"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
