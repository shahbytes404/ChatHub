package com.shahbytes.chathub.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record MessageSearchResultResponse(
        UUID messageId,
        UUID conversationId,
        UUID senderId,
        String content,
        Instant createdAt,
        long sequenceNumber
) {
}
