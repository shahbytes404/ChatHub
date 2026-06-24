package com.shabytes.backend.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterDeviceRequest(
        @NotBlank @Size(max = 100) String deviceId,
        @NotNull DevicePlatform platform,
        @Size(max = 500) String pushToken
) {
}
