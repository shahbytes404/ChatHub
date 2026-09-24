package com.shahbytes.chathub.api.dto;

import com.shahbytes.chathub.domain.type.MessageType;
import com.shahbytes.chathub.domain.type.ReceiptState;

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
        Instant createdAt,
        ReceiptState receiptState
) {
}
