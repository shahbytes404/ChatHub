package com.shabytes.backend.api.dto;

import java.time.Instant;
import java.util.UUID;

public record PresenceResponse(UUID userId,
                               boolean online,
                               Instant lastSeenAt) {

}
