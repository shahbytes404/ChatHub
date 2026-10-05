package com.shahbytes.chathub.api.dto.response;

import java.util.List;

public record MessagePageResponse(
        List<MessageResponse> messages,
        Long nextAfterSequence,
        boolean hasMore
) {
}
