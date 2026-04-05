package com.subhayan.authservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RefreshTokenService refreshTokenService;

    private static final long REFRESH_EXPIRATION = 604800000L;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(redisTemplate, REFRESH_EXPIRATION);
    }

    @Test
    void generateRefreshToken_storesTokenInRedisWithCorrectPrefix() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        String token = refreshTokenService.generateRefreshToken("user-id-123");

        assertThat(token).isNotNull().isNotBlank();
        verify(valueOperations).set(
                eq("refresh:" + token),
                eq("user-id-123"),
                eq(REFRESH_EXPIRATION),
                eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    void getRefreshToken_withExistingToken_returnsUserId() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("refresh:valid-token")).thenReturn("user-id-123");

        String userId = refreshTokenService.getRefreshToken("valid-token");

        assertThat(userId).isEqualTo("user-id-123");
    }

    @Test
    void deleteRefreshToken_deletesWithCorrectKey() {
        refreshTokenService.deleteRefreshToken("token-to-delete");

        verify(redisTemplate).delete("refresh:token-to-delete");
    }
}
