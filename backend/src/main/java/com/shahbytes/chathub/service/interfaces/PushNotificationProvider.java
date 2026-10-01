package com.shahbytes.chathub.service.interfaces;

import com.shahbytes.chathub.domain.DeviceRegistration;

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
