package com.shahbytes.chathub.api.dto.event;

import com.shahbytes.chathub.api.dto.response.MessageResponse;

import java.util.List;
import java.util.UUID;

public record MessageCreatedEvent(
        MessageResponse message,
        List<UUID> recipientIds
) {
}
