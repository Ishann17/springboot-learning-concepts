package com.ishan.user_service.component.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;

/**
 * Low-level Redis access helper.
 * PURPOSE:
 * - Centralizes all direct Redis operations
 * - Hides StringRedisTemplate usage from business logic
 * - Makes Redis interactions explicit and readable
 * DESIGN LEARNING:
 * - Business logic should NOT know how Redis works
 * - It should only express intent (set key, check key, delete key)
 */
@Component
public class RedisStore {

    private final StringRedisTemplate redisTemplate;

    public RedisStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Store a key with a value and TTL.
     *
     * Used for:
     * - job leases
     * - cooldowns
     *
     * TTL ensures Redis auto-cleans stale state.
     * TTL = Time To Live -> “How long something should exist before it disappears automatically.”
     * Example : TTL = 30 seconds, After 30 seconds → Redis deletes it by itself
     */
    public void setWithTtl(String key, String value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    /**
     * Check whether a key exists.
     *
     * Used for:
     * - checking cooldown
     * - checking active job presence
     */
    public boolean exists(String key) {
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * Delete a key explicitly.
     *
     * Used when:
     * - job finishes successfully
     * - cleanup is required before TTL expiry
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
