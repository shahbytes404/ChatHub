package com.shabytes.backend.domain;

import com.shabytes.backend.api.dto.DevicePlatform;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "device_registration")
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

    protected DeviceRegistration() {
    }

    public DeviceRegistration(UUID userId, String deviceId, DevicePlatform platform, String pushToken) {
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
        createdAt = Instant.now();
    }

    public void refresh(DevicePlatform newPlatform, String newPushToken) {
        platform = newPlatform;
        pushToken = newPushToken;
        lastSeenAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public DevicePlatform getPlatform() {
        return platform;
    }

    public String getPushToken() {
        return pushToken;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
