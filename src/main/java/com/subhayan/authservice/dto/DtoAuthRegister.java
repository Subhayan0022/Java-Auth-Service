package com.subhayan.authservice.dto;

import jakarta.validation.constraints.NotNull;

public class DtoAuthRegister {
    public record UserRegisterRequestDTO(@NotNull String email, @NotNull String password, String role) {}

    public record UserRegisterResponseDTO(java.util.UUID id, String email, String role) {}
}
