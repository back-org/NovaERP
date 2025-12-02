package com.novaerp.security;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RedisTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String PREFIX = "blacklisted_token:";

    public RedisTokenService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Blacklist token until expiry (duration in ms).
     */
    public void blacklistToken(String token, long ttlMillis) {
        String key = PREFIX + token;
        // store any value, set TTL
        redisTemplate.opsForValue().set(key, "blacklisted", ttlMillis, TimeUnit.MILLISECONDS);
    }

    public boolean isBlacklisted(String token) {
        String key = PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
