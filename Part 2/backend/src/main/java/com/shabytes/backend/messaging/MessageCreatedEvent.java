package com.shabytes.backend.messaging;

import com.shabytes.backend.api.dto.MessageResponse;

import java.util.List;
import java.util.UUID;

public record MessageCreatedEvent(
        MessageResponse message,
        List<UUID> recipientIds
) {
}
