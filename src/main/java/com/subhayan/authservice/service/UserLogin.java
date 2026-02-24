package com.subhayan.authservice.service;

import com.subhayan.authservice.dto.DtoAuthLogin.*;
import com.subhayan.authservice.entity.UserEntity;
import com.subhayan.authservice.exception.InvalidCredentialsException;
import com.subhayan.authservice.repository.UserRepository;
import com.subhayan.authservice.security.JwtUtil;
import com.subhayan.authservice.security.RefreshTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserLogin {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public UserLogin(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,  RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    public AuthResponse loginUser(LoginRequest loginRequest) {
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

        String userId = user.getId().toString();
        String accessToken = jwtUtil.generateToken(userId);
        String refreshToken = refreshTokenService.generateRefreshToken(userId);
        log.debug("Generated JWT token: {}", accessToken);
        return new AuthResponse(accessToken, refreshToken);
    }
}
