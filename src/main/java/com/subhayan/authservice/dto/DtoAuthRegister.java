package com.subhayan.authservice.dto;

import com.subhayan.authservice.entity.Role;
import com.subhayan.authservice.entity.Salutation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public class DtoAuthRegister {
    public record UserRegisterRequestDTO(
            @NotBlank Salutation salutation,
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8) String password,
            @NotBlank String phoneNumber,
            @NotBlank LocalDate dateOfBirth

    ) {}

    public record UserRegisterResponseDTO(
            UUID id,
            Salutation salutation,
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            LocalDate dateOfBirth,
            Role role
    ) {}
}
