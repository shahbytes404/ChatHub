package com.shahbytes.chathub.service;

import com.shahbytes.chathub.api.dto.event.RealtimeEvent;
import com.shahbytes.chathub.api.dto.response.ConversationResponse;
import com.shahbytes.chathub.domain.Conversation;
import com.shahbytes.chathub.domain.OutboxEvent;
import com.shahbytes.chathub.domain.type.EventType;
import com.shahbytes.chathub.domain.type.ResourceType;
import com.shahbytes.chathub.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationEventService {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public void conversationChangedForMembers(
            List<UUID> recipientIds,
            UUID conversationId,
            UUID actorId,
            EventType eventType,
            ConversationResponse conversation
    ) {
        for (var recipientId : recipientIds) {
            save(
                    new RealtimeEvent(
                            eventType,
                            recipientId,
                            conversationId,
                            actorId,
                            null,
                            conversationId,
                            Instant.now()
                    )
            );
        }
    }

    public void memberRemoved(
            UUID targetUserId,
            UUID conversationId,
            UUID actorId
    ) {
        save(
                new RealtimeEvent(
                        EventType.CONVERSATION_REMOVED,
                        targetUserId,
                        conversationId,
                        actorId,
                        null,
                        Map.of(
                                "conversationId", conversationId,
                                "removed", true
                        ),
                        Instant.now()
                )
        );
    }

    public void conversationRemoved(
            UUID targetUserId,
            UUID conversationId,
            UUID actorId,
            boolean permanent
    ) {
        save(
                new RealtimeEvent(
                        EventType.CONVERSATION_REMOVED,
                        targetUserId,
                        conversationId,
                        actorId,
                        null,
                        Map.of(
                                "conversationId", conversationId,
                                "permanent", permanent
                        ),
                        Instant.now()
                )
        );
    }

    private void save(RealtimeEvent event) {
        var outboxEvent = new OutboxEvent(
                ResourceType.CONVERSATION.name(),
                event.conversationId(),
                event.type().name(),
                toJson(event)
        );

        outboxRepository.save(outboxEvent);
    }

    private String toJson(RealtimeEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Could not serialize conversation event", exception);
        }
    }
}
