package com.shabytes.backend.service;

import com.shabytes.backend.exception.RateLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimitService {
    private final StringRedisTemplate redisTemplate;
    private final int messagesPerMinute;
    private final ConcurrentHashMap<String, AtomicInteger> localFallback = new ConcurrentHashMap<>();

    public RateLimitService(
            StringRedisTemplate redisTemplate,
            @Value("${chathub.rate-limit.messages-per-minute}") int messagesPerMinute
    ) {
        this.redisTemplate = redisTemplate;
        this.messagesPerMinute = messagesPerMinute;
    }


    public void checkMessageSend(UUID userId) {
        // 10:30:00 to 10:30:59 -> one bucket
        // 10:31:00 to 10:31:59 -> next bucket
        var minute = Instant.now().getEpochSecond() / 60;
        // rate:message:estdrfyguyhutedxrsfjyhudryjt:243576788
        var key = "rate:message:" + userId + ":" + minute;
        long count;

        try {
            // Redis INCR
            // First message -> counter = 1
            // Second message -> counter = 2
            // 61st message -> counter = 61
            var redisCount = redisTemplate.opsForValue().increment(key);
            if (redisCount != null && redisCount == 1) {
                redisTemplate.expire(key, Duration.ofMinutes(2));
            }
            count = redisCount == null ? 0 : redisCount;
        } catch (DataAccessException redisUnavailable) {
            localFallback.keySet().removeIf(existing ->
                    !existing.endsWith(":" + minute));
            count = localFallback.computeIfAbsent(key, ignored -> new AtomicInteger()).incrementAndGet();
        }

        if (count > messagesPerMinute) {
            throw new RateLimitExceededException("Message rate limit exceeded");
        }
    }
}
