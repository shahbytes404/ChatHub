package com.shahbytes.chathub.api.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateConversationRequest(
        @Size(max = 120) String title
) {
}
