package com.shahbytes.chathub.api;

import com.shahbytes.chathub.api.dto.request.RegisterDeviceRequest;
import com.shahbytes.chathub.security.CurrentUser;
import com.shahbytes.chathub.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final CurrentUser currentUser;

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void register(
            Authentication authentication,
            @Valid @RequestBody RegisterDeviceRequest request
    ) {
        deviceService.register(
                currentUser.id(authentication),
                request
        );
    }
}
