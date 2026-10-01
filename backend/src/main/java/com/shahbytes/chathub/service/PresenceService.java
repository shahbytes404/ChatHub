package com.shahbytes.chathub.service;

import com.shahbytes.chathub.api.dto.response.PresenceResponse;
import com.shahbytes.chathub.repository.ConversationMemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PresenceService {
    private final StringRedisTemplate redisTemplate;

    private final ConversationMemberRepository memberRepository;

    private final Duration ttl;

    public PresenceService(
            StringRedisTemplate redisTemplate,
            ConversationMemberRepository memberRepository,
            @Value("${chathub.presence.ttl}")
            Duration ttl) {
        this.redisTemplate = redisTemplate;
        this.memberRepository = memberRepository;
        this.ttl = ttl;
    }

    public void connected(
            UUID userId,
            String sessionId
    ) {
        try {
            var key = sessionKey(userId);
            redisTemplate.opsForSet().add(key, sessionId);

            redisTemplate.expire(key, ttl);
        } catch (DataAccessException ignored) {

        }
    }

    public boolean isOnline(UUID userId) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey(userId)));
        } catch (DataAccessException ex) {
            return false;
        }
    }

    public PresenceResponse get(UUID userId) {
        try {
            var online = isOnline(userId);

            var rawLastSeen = redisTemplate.opsForValue().get(lastSeenKey(userId));

            var lastSeen = rawLastSeen == null ? null : Instant.parse(rawLastSeen);

            return new PresenceResponse(
                    userId,
                    online,
                    lastSeen
            );

        } catch (DataAccessException ex) {
            return new PresenceResponse(
                    userId,
                    false,
                    null
            );
        }
    }

    public List<PresenceResponse> list(UUID conversationId) {
        return memberRepository
                .findAllByConversationId(conversationId)
                .stream()
                .map(member -> get(member.getUserId()))
                .toList();
    }

    public void heartbeat(
            UUID userId,
            String sessionId
    ) {
        connected(userId, sessionId);
    }

    public void disconnected(
            UUID userId,
            String sessionId
    ) {
        try {
            var key = sessionKey(userId);

            redisTemplate.opsForSet().remove(key, sessionId);

            var remaining = redisTemplate.opsForSet().size(key);

            if (remaining == null || remaining == 0) {
                redisTemplate.delete(key);

                redisTemplate.opsForValue().set(
                        lastSeenKey(userId),
                        Instant.now().toString());
            }

        } catch (DataAccessException ex) {

        }
    }

    private String sessionKey(UUID userId) {
        return "presence:user:" + userId + ":sessions";
    }

    private String lastSeenKey(UUID userId) {
        return "presence:user:" + userId + ":last-seen";
    }
}
