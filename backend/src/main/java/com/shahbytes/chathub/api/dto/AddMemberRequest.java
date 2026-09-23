package com.shahbytes.chathub.api.dto;

import com.shahbytes.chathub.domain.MemberRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddMemberRequest(
        @NotNull UUID userId,
        @NotNull MemberRole role
) {
}
