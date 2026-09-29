package com.shahbytes.chathub.service;

/*
KEY                              VALUE
rate:message:user-A:287871         6
rate:message:user-A:387871         6
rate:message:user-A:787871         6
rate:message:user-B:299787         7
rate:message:user-C:223787         47

1 -> INCR -> 2
 */

import com.shahbytes.chathub.exception.RateLimitExceededException;
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

    private final StringRedisTemplate stringRedisTemplate;
    private final int messagePerMinute;
    private final ConcurrentHashMap<String, AtomicInteger> localFallback =
            new ConcurrentHashMap<>();

    public RateLimitService(
            StringRedisTemplate stringRedisTemplate,
            @Value("${chathub.rate-limit.messages-per-minute}")
            int messagePerMinute) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.messagePerMinute = messagePerMinute;
    }

    public void checkMessageSend(UUID userId) {
        /*
        10:30:00 -> 10:30:59 -> one redis counter -> one bucket

        10:31:00 -> 10:31:59 -> another redis counter ->next bucket
         */

        Instant now = Instant.now();

        long minute = now.getEpochSecond() / 60;

        String key = "rate:message:" + userId + ":" + minute;

        /*
        INCR is atomic

        First message:
        0 -> 1

        Second
        1 -> 2

        ...

        61st:
            60 -> 61

         */
        long count;

        try {
            var redisCount = stringRedisTemplate.opsForValue().increment(key);

            if (redisCount == null) {
                throw new IllegalStateException(
                        "Could not increment Redis rate-limit counter"
                );
            }

            if (redisCount == 1) {
            /*
            EXPIRE key
             */
                stringRedisTemplate.expire(
                        key,
                        Duration.ofMinutes(2)
                );
            }

            count = redisCount;
        } catch (DataAccessException redisUnavailable) {
            localFallback.keySet().removeIf(existing ->
                    !existing.endsWith(":" + minute));

            count = localFallback.computeIfAbsent(key, ignored -> new AtomicInteger())
                    .incrementAndGet();
        }

        if (count > messagePerMinute) {
            throw new RateLimitExceededException(
                    "Message rate limit exceeded"
            );
        }
    }
}
