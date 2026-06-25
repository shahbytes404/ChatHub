package com.shabytes.backend.service;

import com.shabytes.backend.api.dto.PresenceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class PresenceService {
    private final StringRedisTemplate stringRedisTemplate;

    private final Duration ttl;

    public PresenceService(StringRedisTemplate stringRedisTemplate,
                           @Value("${chathub.presence.ttl}") Duration ttl, RedisTemplate<Object, Object> redisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.ttl = ttl;
    }

    public void connected(UUID userId, String sessionId) {
        try {
            var key = sessionKey(userId);
            stringRedisTemplate.opsForSet().add(key, sessionId);

            // 10:00
            // ttl is 75 seconds
            stringRedisTemplate.expire(key, ttl);
        } catch (DataAccessException ignored) {

        }
    }

    public boolean isOnline(UUID userId) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(sessionKey(userId)));
        } catch (DataAccessException redisUnavailable) {
            return false;
        }
    }

    public PresenceResponse get(UUID uuid) {
        try {
            var online = isOnline(uuid);
            var rawLastSeen = stringRedisTemplate.opsForValue().get(lastSeenKey(uuid));
            var lastSeen = rawLastSeen == null ? null : Instant.parse(rawLastSeen);
            return new PresenceResponse(uuid, online, lastSeen);
        } catch (DataAccessException dataAccessException) {
            return new PresenceResponse(uuid, false, null);
        }
    }

    public void heartbeat(UUID userId, String sessionId) {
        connected(userId, sessionId);
    }

    public void disconnected(UUID userId, String sessionId) {
        try {
            var key = sessionKey(userId);
            stringRedisTemplate.opsForSet().remove(key, sessionId);

            var remaining = stringRedisTemplate.opsForSet().size(key);
            if (remaining == null || remaining == 0) {
                stringRedisTemplate.delete(key);
                stringRedisTemplate.opsForValue().set(lastSeenKey(userId), Instant.now().toString());
            }
        } catch (DataAccessException ignored) {

        }
    }

    private String lastSeenKey(UUID userId) {
        return "presence:user:" + userId + ":last-seen";
    }

    private String sessionKey(UUID userId) {
        return "presence:user:" + userId + ":sessions";
    }
}
