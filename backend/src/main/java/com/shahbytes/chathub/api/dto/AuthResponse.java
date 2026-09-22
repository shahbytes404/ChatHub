package com.shahbytes.chathub.api.dto;

import java.time.Instant;
import java.util.UUID;

public record AuthResponse(
        String accessToken, String tokenType, Instant expiresAt, UUID userId,
        String displayName, String email) {
}
