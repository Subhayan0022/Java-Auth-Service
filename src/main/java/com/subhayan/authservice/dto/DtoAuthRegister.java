package com.subhayan.authservice.dto;

import com.subhayan.authservice.entity.Role;
import com.subhayan.authservice.entity.Salutation;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

public class DtoAuthRegister {
    public record UserRegisterRequestDTO(
            @NotNull Salutation salutation,
            @NotBlank @Size(max = 100) String firstName,
            @NotBlank @Size(max = 100) String lastName,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8) String password,
            @NotBlank @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number format") String phoneNumber, // Valid Phone numbers format
            @NotNull LocalDate dateOfBirth

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
