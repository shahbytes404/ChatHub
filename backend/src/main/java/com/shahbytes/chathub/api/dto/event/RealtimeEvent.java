package com.shahbytes.chathub.api.dto.event;

import java.time.Instant;
import java.util.UUID;

public record RealtimeEvent(
        String type,
        UUID targetUserId,
        UUID conversationId,
        UUID actorUserId,
        UUID messageId,
        Object payload,
        Instant occurredAt
) {
}
