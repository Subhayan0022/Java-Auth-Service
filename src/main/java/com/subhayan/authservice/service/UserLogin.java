package com.subhayan.authservice.service;

import com.subhayan.authservice.dto.DtoAuthLogin.*;
import com.subhayan.authservice.entity.UserEntity;
import com.subhayan.authservice.exception.InvalidCredentialsException;
import com.subhayan.authservice.repository.UserRepository;
import com.subhayan.authservice.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserLogin {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserLogin(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public JwtResponse loginUser(@NonNull LoginRequest loginRequest) {
        // Check if the user email in login requests exists in DB.
        UserEntity user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> {
                    log.warn("{} not found. No accounts exist with this email", loginRequest.email());
                    return new InvalidCredentialsException("Invalid email or password");
                });

        // Then check the if the decoded password in DB matches the request.
        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            log.warn("{} password does not match stored value", loginRequest.email());
            throw new InvalidCredentialsException("Invalid email or password"); // For security, be obscure.
        }

        String jwtToken = jwtUtil.generateToken(user.getEmail());
        log.debug("Generated JWT token: {}", jwtToken);
        return new JwtResponse(jwtToken);
    }
}
