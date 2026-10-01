package com.shahbytes.chathub.api.dto.request;

import jakarta.validation.constraints.NotNull;

public record TypingEvent(@NotNull Boolean typing) {
}
