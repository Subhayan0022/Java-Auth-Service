package com.subhayan.authservice.dto;

import com.subhayan.authservice.entity.Role;
import com.subhayan.authservice.entity.Salutation;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserDetailsResponse(
        UUID id,
        Salutation salutation,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        LocalDate dateOfBirth,
        Role role,
        LocalDateTime createdAt
) implements Serializable {}
