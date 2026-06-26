package com.shabytes.backend.service;

import com.shabytes.backend.domain.DeviceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LoggingPushNotification implements PushNotificationProvider {
    private static final Logger log = LoggerFactory.getLogger(LoggingPushNotification.class);

    @Override
    public void send(DeviceRegistration device, UUID conversationId, UUID messageId, String title, String body) {
        log.info(
                "Push notification queued platform={} deviceId={} conversationId={} messageId={}",
                device.getPlatform(),
                device.getDeviceId(),
                conversationId,
                messageId
        );
    }
}
