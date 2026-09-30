package com.shahbytes.chathub.messaging;

import com.shahbytes.chathub.api.dto.event.RealtimeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class RedisRealtimeSubscriber implements MessageListener {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(
            Message message,
            byte[] pattern
    ) {
        try {
            var event = objectMapper.readValue(
                    new String(message.getBody(),
                            StandardCharsets.UTF_8),
                    RealtimeEvent.class
            );

            simpMessagingTemplate.convertAndSendToUser(
                    event.targetUserId().toString(),
                    "/queue/events",
                    event
            );
        } catch (JacksonException exception) {
            // Ignore malformed realtime messages for now.
        }

        // /user/queue/events

        /*
        User A = 111
        User B = 222

        targetUserId = 222


         */
    }
}
