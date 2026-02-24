package com.subhayan.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DtoAuthLogin {
    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8) String password) {
    }

    // Simply return the token here.
    public record AuthResponse(String token, String refreshToken) {}

    public record RefreshRequest(
            @NotBlank String refreshToken) {}
}
