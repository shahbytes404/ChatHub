package com.shahbytes.chathub.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
        @NotBlank @Size(max = 100) String clientMessageId,
        @NotBlank @Size(max = 4000) String content
) {
}
