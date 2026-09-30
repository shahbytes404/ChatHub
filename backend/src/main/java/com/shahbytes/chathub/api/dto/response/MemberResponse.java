package com.shahbytes.chathub.api.dto.response;

import com.shahbytes.chathub.domain.type.MemberRole;

import java.util.UUID;

public record MemberResponse(
        UUID userId,
        String displayName,
        MemberRole role,
        long lastReadSequence
) {
}
