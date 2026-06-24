package com.shabytes.backend.api.dto;

import java.time.Instant;
import java.util.UUID;

public record ReceiptResponse(
        UUID messageId,
        UUID userId,
        Instant deliveredAt,
        Instant readAt
) {
}
