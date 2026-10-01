package com.shahbytes.chathub.service;

import com.shahbytes.chathub.domain.DeviceRegistration;
import com.shahbytes.chathub.service.interfaces.PushNotificationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LoggingPushNotification implements PushNotificationProvider {
    private final Logger logger = LoggerFactory.getLogger(LoggingPushNotification.class);

    @Override
    public void send(DeviceRegistration device, UUID conversationId, UUID messageId, String title, String body) {
        logger.info(
                "Push notification queued platform={} deviceId={} conversationId={} messageId={}",
                device.getPlatform(),
                device.getDeviceId(),
                conversationId,
                messageId
        );
    }
}
