package com.subhayan.authservice.dto;

import com.subhayan.authservice.entity.Role;
import com.subhayan.authservice.entity.Salutation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AdminUpdateRequest (
        Salutation salutation,
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        @Email String email,
        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number format") String phoneNumber,
        LocalDate dateOfBirth,
        Role role
) {}
