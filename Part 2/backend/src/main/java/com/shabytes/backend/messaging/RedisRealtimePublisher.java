package com.shabytes.backend.messaging;

import com.shabytes.backend.api.dto.RealtimeEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class RedisRealtimePublisher {
    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    private final String channel;

    public RedisRealtimePublisher(
            StringRedisTemplate redisTemplate, ObjectMapper objectMapper,
            @Value("${chathub.messaging.realtime-channel}") String channel) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.channel = channel;
    }

    public void publish(RealtimeEvent event) {
        try {
            var payload = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(channel, payload);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Could not serialize realtime event", exception);
        }
    }
}
