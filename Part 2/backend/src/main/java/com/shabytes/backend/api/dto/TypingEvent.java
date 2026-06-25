package com.shabytes.backend.api.dto;

import jakarta.validation.constraints.NotNull;

public record TypingEvent(@NotNull Boolean typing) {
}
