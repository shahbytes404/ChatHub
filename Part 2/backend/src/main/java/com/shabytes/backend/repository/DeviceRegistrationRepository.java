package com.shabytes.backend.repository;

import com.shabytes.backend.domain.DeviceRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceRegistrationRepository extends JpaRepository<DeviceRegistration, UUID> {
    Optional<DeviceRegistration> findByUserIdAndDeviceId(UUID userId, String deviceId);

    List<DeviceRegistration> findAllByUserIdAndNotificationsEnabledTrue(UUID userId);
}
