package com.shabytes.backend.api.dto;

import com.shabytes.backend.domain.MemberRole;

import java.util.UUID;

public record MemberResponse(UUID userId,
                             String displayName,
                             MemberRole role,
                             long lastReadSequence) {
}
