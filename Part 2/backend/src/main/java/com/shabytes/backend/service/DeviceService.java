package com.shabytes.backend.service;

import com.shabytes.backend.api.dto.RegisterDeviceRequest;
import com.shabytes.backend.domain.DeviceRegistration;
import com.shabytes.backend.repository.DeviceRegistrationRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeviceService {
    private final DeviceRegistrationRepository registrationRepository;

    public DeviceService(DeviceRegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    public void register(UUID id, @Valid RegisterDeviceRequest request) {
        var registration = registrationRepository.findByUserIdAndDeviceId(id, request.deviceId())
                .orElseGet(() -> new DeviceRegistration(id,
                        request.deviceId(),
                        request.platform(),
                        request.pushToken()));
        registration.refresh(request.platform(), request.pushToken());
        registrationRepository.save(registration);
    }
}
