package com.subhayan.authservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenService {
    private static final String PREFIX = "refresh:";

    private final StringRedisTemplate redisTemplate;
    private final long refreshExpiration;

    public RefreshTokenService(StringRedisTemplate redisTemplate, @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        this.redisTemplate = redisTemplate;
        this.refreshExpiration = refreshExpiration;
    }

    public String generateRefreshToken(String userId){
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(PREFIX + token, userId, refreshExpiration, TimeUnit.MILLISECONDS);
        return token;
    }

    public String getRefreshToken(String refreshToken){
        return redisTemplate.opsForValue().get(PREFIX + refreshToken);
    }

    public void deleteRefreshToken(String refreshToken){
        redisTemplate.delete(PREFIX + refreshToken);
    }

}
