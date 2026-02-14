package com.subhayan.authservice.dto;

import com.subhayan.authservice.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class DtoAuthRegister {
    public record UserRegisterRequestDTO(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8) String password
    ) {}

    public record UserRegisterResponseDTO(UUID id, String email, Role role) {}
}
