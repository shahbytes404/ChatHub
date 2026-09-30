package com.shahbytes.chathub.api.dto.response;

import com.shahbytes.chathub.domain.type.ReceiptState;

import java.time.Instant;
import java.util.UUID;

public record ReceiptResponse(
        UUID messageId,
        UUID userId,
        Instant deliveredAt,
        Instant readAt,
        ReceiptState receiptState
) {
}
