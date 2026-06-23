package com.shabytes.backend.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BlockUserRequest(@NotNull UUID userId) {
}
