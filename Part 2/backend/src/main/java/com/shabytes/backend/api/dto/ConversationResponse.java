package com.shabytes.backend.api.dto;

import com.shabytes.backend.domain.ConversationType;
import com.shabytes.backend.domain.MemberRole;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ConversationResponse(UUID id,
                                   ConversationType type,
                                   String title,
                                   UUID createdBy,
                                   Instant createdAt,
                                   List<MemberResponse> members
) {
}
