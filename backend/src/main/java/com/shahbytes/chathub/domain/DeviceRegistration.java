package com.shahbytes.chathub.domain;

import com.shahbytes.chathub.api.dto.request.DevicePlatform;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "device_registration")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceRegistration {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "device_id", nullable = false, length = 100)
    private String deviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DevicePlatform platform;

    @Column(name = "push_token", length = 500)
    private String pushToken;

    @Column(name = "notifications_enabled", nullable = false)
    private boolean notificationsEnabled;

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public DeviceRegistration(
            UUID userId,
            String deviceId,
            DevicePlatform platform,
            String pushToken
    ) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.deviceId = deviceId;
        this.platform = platform;
        this.pushToken = pushToken;
        this.notificationsEnabled = true;
        this.lastSeenAt = Instant.now();
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public void refresh(
            DevicePlatform newPlatform,
            String newPushToken
    ) {
        platform = newPlatform;
        pushToken = newPushToken;
        lastSeenAt = Instant.now();
    }

}
