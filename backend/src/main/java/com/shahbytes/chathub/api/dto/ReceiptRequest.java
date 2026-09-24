package com.shahbytes.chathub.api.dto;

import com.shahbytes.chathub.domain.type.ReceiptType;
import jakarta.validation.constraints.NotNull;

public record ReceiptRequest(
        @NotNull ReceiptType type
) {
}
