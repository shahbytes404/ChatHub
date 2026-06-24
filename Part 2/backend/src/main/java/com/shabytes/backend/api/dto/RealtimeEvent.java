package com.shabytes.backend.api.dto;

import java.time.Instant;
import java.util.UUID;

public record RealtimeEvent(
        String type,
        UUID targetUserId,
        UUID conversationId,
        UUID actorUserId,
        UUID messageID,
        Object payload,
        Instant occurredAt
) {
}
