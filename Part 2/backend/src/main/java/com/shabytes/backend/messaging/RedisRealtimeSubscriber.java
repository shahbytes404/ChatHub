package com.shabytes.backend.messaging;

import com.shabytes.backend.api.dto.RealtimeEvent;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@Component
public class RedisRealtimeSubscriber implements MessageListener {
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public RedisRealtimeSubscriber(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onMessage(Message message, byte @Nullable [] pattern) {
        try {
            var event = objectMapper.readValue(
                    new String(message.getBody(), StandardCharsets.UTF_8), RealtimeEvent.class
            );
            // /user/queue/events
            messagingTemplate.convertAndSendToUser(
                    event.targetUserId().toString(),
                    "/queue/events",
                    event
            );
        } catch (JacksonException ignored) {

        }
    }
}
