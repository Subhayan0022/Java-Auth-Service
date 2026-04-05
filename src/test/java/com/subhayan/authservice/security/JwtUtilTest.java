package com.subhayan.authservice.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String testUserId;

    @BeforeEach
    void setUp() {
        String secret = Base64.getEncoder().encodeToString(new byte[32]);
        long expiration = 900000;
        jwtUtil = new JwtUtil(secret, expiration);
        testUserId = UUID.randomUUID().toString();
    }

    @Test
    void getUserIDFromToken_returnsCorrectUserId() {
        String token = jwtUtil.generateToken(testUserId);

        String extractedUserId = jwtUtil.getUserIDFromToken(token);

        assertThat(extractedUserId).isEqualTo(testUserId);
    }

    @Test
    void validateToken_withValidTokenAndCorrectUserId_returnsTrue() {
        String token = jwtUtil.generateToken(testUserId);

        boolean valid = jwtUtil.validateToken(token, testUserId);

        assertThat(valid).isTrue();
    }

    @Test
    void validateToken_withValidTokenAndWrongUserId_returnsFalse() {
        String token = jwtUtil.generateToken(testUserId);
        String differentUserId = UUID.randomUUID().toString();

        boolean valid = jwtUtil.validateToken(token, differentUserId);

        assertThat(valid).isFalse();
    }

    @Test
    void expiredToken_throwsExpiredJwtException() {
        String secret = Base64.getEncoder().encodeToString(new byte[32]);
        JwtUtil expiredJwtUtil = new JwtUtil(secret, 0);

        String token = expiredJwtUtil.generateToken(testUserId);

        assertThatThrownBy(() -> expiredJwtUtil.parseToken(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void parseToken_withTamperedToken_throwsException() {
        String token = jwtUtil.generateToken(testUserId);
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThatThrownBy(() -> jwtUtil.parseToken(tampered))
                .isInstanceOf(Exception.class);
    }
}
