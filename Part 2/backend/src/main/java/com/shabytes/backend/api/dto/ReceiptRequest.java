package com.shabytes.backend.api.dto;

import jakarta.validation.constraints.NotNull;

public record ReceiptRequest(
        @NotNull ReceiptType type
) {
}
