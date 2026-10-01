package com.shahbytes.chathub.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record PresenceResponse(
        UUID userId,
        boolean online,
        Instant lastSeenAt
) {
}
