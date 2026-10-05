package com.shahbytes.chathub.service;

import com.shahbytes.chathub.api.dto.request.RegisterDeviceRequest;
import com.shahbytes.chathub.domain.DeviceRegistration;
import com.shahbytes.chathub.repository.DeviceRegistrationRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRegistrationRepository registrationRepository;

    public void register(
            UUID userId,
            @Valid RegisterDeviceRequest request
    ) {
        var registration = registrationRepository
                .findByUserIdAndDeviceId(userId, request.deviceId())
                .orElseGet(() -> new DeviceRegistration(
                        userId,
                        request.deviceId(),
                        request.platform(),
                        request.pushToken()
                ));

        registration.refresh(request.platform(), request.pushToken());

        registrationRepository.save(registration);
    }
}
