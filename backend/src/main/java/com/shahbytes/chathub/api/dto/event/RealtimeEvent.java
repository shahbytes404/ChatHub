package com.shahbytes.chathub.api.dto.event;

import com.shahbytes.chathub.domain.type.EventType;

import java.time.Instant;
import java.util.UUID;

public record RealtimeEvent(
        EventType type,
        UUID targetUserId,
        UUID conversationId,
        UUID actorUserId,
        UUID messageId,
        Object payload,
        Instant occurredAt
) {
}
