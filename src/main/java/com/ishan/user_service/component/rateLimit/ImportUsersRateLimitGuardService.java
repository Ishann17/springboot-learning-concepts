package com.ishan.user_service.component.rateLimit;

import com.ishan.user_service.component.redis.RedisStore;
import com.ishan.user_service.customExceptions.TooManyRequestsException;
import com.ishan.user_service.utility.redis.RedisKeysGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Core decision engine for import job rate limiting.
 * Purpose:
 * - Decides if a user is allowed to start a new import job.
 * - Enforces concurrency limits per cost tier.
 * - Enforces cooldown after XL jobs.
 * Why Needed:
 * - Protects DB + CPU from overload.
 * - Ensures multi-tenant fairness.
 * Design:
 * - In-memory state (for now)
 * - Thread-safe storage using ConcurrentHashMap
 * Future:
 * - Can be replaced with Redis / DB backed state for distributed systems.
 */
@Service
public class ImportUsersRateLimitGuardService {

    private final Logger log = LoggerFactory.getLogger(ImportUsersRateLimitGuardService.class);

    private final Map<String, UserRateState> userStateMap = new ConcurrentHashMap<>();

    private final RedisStore redisStore;

    public ImportUsersRateLimitGuardService(RedisStore redisStore) {
        this.redisStore = redisStore;
    }


    /**
     * MAIN ENTRY — decides if a job is allowed.
     *
     * Order matters:
     * Check GLOBAL cooldown (XL protection)
     * Check tier concurrency
     */
    public void checkIfAllowed(String userId, long count){

        checkCooldown(userId);
        ImportJobCostTier tier = ImportJobCostTier.fromCount(count);
        UserRateState state = getOrCreateState(userId);


        //GLOBAL cooldown check
        LocalDateTime allowedAt = state.getNextImportAllowedAt();

        if(allowedAt != null && LocalDateTime.now().isBefore(allowedAt)){
            throw new TooManyRequestsException(
                    "You must wait until " + allowedAt + " before starting another import."
            );
        }

        // Tier concurrency check
        AtomicInteger running = state.getRunningJobs()
                        .computeIfAbsent(tier, t -> new AtomicInteger(0));

        if(running.get() >= tier.getMaxConcurrentJobs()){
            throw new TooManyRequestsException(
                    tier.name() + " concurrency limit reached. Max allowed = "
                            + tier.getMaxConcurrentJobs()
            );
        }
    }



    /**
     * MUST be called once job is ACCEPTED.
     * Uses atomic increment → thread safe.
     */
    public void markJobStarted(String userId, String jobId, ImportJobCostTier tier){

        UserRateState state = getOrCreateState(userId);

        String jobKey = RedisKeysGenerator.jobKey(jobId);
        // lease for the job; pick a safe max duration (e.g., 30 minutes for now)
        redisStore.setWithTtl(jobKey, tier.name(), Duration.ofMinutes(30));

        state.getRunningJobs()
                .computeIfAbsent(tier, t -> new AtomicInteger(0))
                .incrementAndGet();

        log.info("[RATE LIMIT GUARD]|User={} Tier={} Running={}",
                userId,
                tier,
                state.getRunningJobs().get(tier).get());
    }



    /**
     * MUST be called in ASYNC finally block.
     *
     * Handles:
     * decrement
     * XL cooldown
     */
    public void markJobFinished(String userId, String jobId, ImportJobCostTier tier){

        UserRateState state = getOrCreateState(userId);

        AtomicInteger running = state.getRunningJobs().get(tier);

        String jobKey = RedisKeysGenerator.jobKey(jobId);
        redisStore.delete(jobKey);


        if(running != null){
            running.decrementAndGet();
        }

        // We are moving shared runtime state from JVM memory to Redis.
        if(tier == ImportJobCostTier.XL){
            String cooldownKey = RedisKeysGenerator.cooldownKey(userId);
            // 30 seconds cooldown
            redisStore.setWithTtl(cooldownKey, "true", Duration.ofSeconds(30));
        }
    }

    private UserRateState getOrCreateState(String userId){
        return userStateMap.computeIfAbsent(userId, id -> new UserRateState());
    }

    /**
     * Checks whether the user is currently in cooldown.
     *
     * PURPOSE:
     * - Enforces mandatory wait period after XL jobs
     * - Uses Redis TTL to auto-expire cooldown
     *
     * DESIGN LEARNING:
     * - Cooldown is shared, time-based state
     * - Redis is the source of truth, not memory
     */
    private void checkCooldown(String userId) {
        String cooldownKey = RedisKeysGenerator.cooldownKey(userId);

        if (redisStore.exists(cooldownKey)) {
            throw new TooManyRequestsException(
                    "User is in cooldown period. Please try again later."
            );
        }
    }

}

