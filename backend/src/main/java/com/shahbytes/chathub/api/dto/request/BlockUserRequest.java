package com.shahbytes.chathub.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BlockUserRequest(@NotNull UUID userId) {
}
