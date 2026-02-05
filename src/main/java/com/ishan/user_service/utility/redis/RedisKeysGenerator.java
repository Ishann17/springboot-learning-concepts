package com.ishan.user_service.utility.redis;

/**
 * Central place for all Redis key formats used in the system.
 *
 * WHY THIS EXISTS:
 * - Redis keys are plain strings → easy to typo, hard to refactor
 * - This class provides ONE source of truth for key structure
 * - Makes Redis debugging, refactoring, and reasoning predictable
 *
 * DESIGN LEARNING:
 * - Redis stores STATE, not rules
 * - Keys must encode intent clearly (user, job, cooldown, etc.)
 * - Never scatter string keys across business logic
 */
public final class RedisKeysGenerator {

    /**
     * Utility class → should never be instantiated.
     * Private constructor enforces correct usage.
     */
    private RedisKeysGenerator() {}

    /**
     * Key representing running job state for a user.
     *
     * Example:
     * user:vasu:runningJobs
     *
     * PURPOSE:
     * - Holds per-user concurrency information
     * - Shared across all application instances
     * - Source of truth for rate limiting decisions
     */
    public static String runningJobsKey(String userId) {
        return "user:" + userId + ":runningJobs";
    }

    /**
     * Key representing a single job lease.
     *
     * Example:
     * job:3f2a9c9e-1b7c-4a0e-bb8a-123456
     *
     * PURPOSE:
     * - Each job gets its own Redis entry
     * - TTL on this key acts as a safety net
     * - If a pod crashes, Redis auto-cleans stale jobs
     */
    public static String jobKey(String jobId) {
        return "job:" + jobId;
    }

    /**
     * Key representing cooldown state after XL job completion.
     *
     * Example:
     * user:vasu:cooldown
     *
     * PURPOSE:
     * - Enforces "wait period" after XL jobs
     * - TTL-based → automatically expires
     * - Must be visible across all pods
     */
    public static String cooldownKey(String userId) {
        return "user:" + userId + ":cooldown";
    }

}
