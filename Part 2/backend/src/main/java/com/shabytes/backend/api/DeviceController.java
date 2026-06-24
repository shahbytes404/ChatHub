package com.shabytes.backend.api;

import com.shabytes.backend.api.dto.RegisterDeviceRequest;
import com.shabytes.backend.security.CurrentUser;
import com.shabytes.backend.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;
    private final CurrentUser currentUser;

    public DeviceController(DeviceService deviceService, CurrentUser currentUser) {
        this.deviceService = deviceService;
        this.currentUser = currentUser;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void register(
            Authentication authentication,
            @Valid @RequestBody RegisterDeviceRequest request
    ) {
        deviceService.register(currentUser.id(authentication), request);
    }
}
