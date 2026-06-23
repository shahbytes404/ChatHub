package com.shabytes.backend.api.dto;

import com.shabytes.backend.domain.MemberRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddMemberRequest(
        @NotNull UUID userId,
        @NotNull MemberRole role
) {
}
