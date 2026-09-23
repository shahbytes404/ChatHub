package com.shahbytes.chathub.api.dto;

import com.shahbytes.chathub.domain.ConversationType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ConversationResponse(
        UUID id,
        ConversationType type,
        String title,
        UUID createdBy,
        Instant createdAt,
        List<MemberResponse> members
) {
}
