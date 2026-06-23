package com.shabytes.backend.api.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID conversationId,
        UUID senderId,
        String clientMessageId,
        long sequenceNumber,
        MessageType type,
        String content,
        Instant createdAt

) {
}
