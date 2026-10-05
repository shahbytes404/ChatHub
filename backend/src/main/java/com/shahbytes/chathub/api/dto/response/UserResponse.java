package com.shahbytes.chathub.api.dto.response;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String displayName,
        String email
) {
}
