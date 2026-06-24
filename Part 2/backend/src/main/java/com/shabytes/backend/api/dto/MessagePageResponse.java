package com.shabytes.backend.api.dto;

import java.util.List;

public record MessagePageResponse(
        List<MessageResponse> messages,
        Long nextAfterSequence,
        boolean hasMore
) {
}
