package com.shabytes.backend.service;

import com.shabytes.backend.domain.DeviceRegistration;

import java.util.UUID;

public interface PushNotificationProvider {
    void send(
            DeviceRegistration device,
            UUID conversationId,
            UUID messageId,
            String title,
            String body
    );
}
