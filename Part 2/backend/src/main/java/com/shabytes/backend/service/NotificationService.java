package com.shabytes.backend.service;

import com.shabytes.backend.api.dto.MessageResponse;
import com.shabytes.backend.repository.ConversationMemberRepository;
import com.shabytes.backend.repository.DeviceRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class NotificationService {
    private final DeviceRegistrationRepository deviceRepository;
    private final ConversationMemberRepository memberRepository;
    private final PushNotificationProvider pushProvider;

    public NotificationService(DeviceRegistrationRepository deviceRepository, ConversationMemberRepository memberRepository, PushNotificationProvider pushProvider) {
        this.deviceRepository = deviceRepository;
        this.memberRepository = memberRepository;
        this.pushProvider = pushProvider;
    }

    @Transactional(readOnly = true)
    public void notifyOfflineUser(UUID userId, MessageResponse message) {
        var member = memberRepository.
                findByConversationIdAndUserId(message.conversationId(), userId);
        if (member.isEmpty()) {
            return;
        }

        if (member.get().isMutedAt(Instant.now())) {
            return;
        }

        deviceRepository.findAllByUserIdAndNotificationsEnabledTrue(userId).stream()
                .filter(device -> device.getPushToken() != null && !device.getPushToken().isBlank())
                .forEach(device -> pushProvider.send(
                        device,
                        message.conversationId(),
                        message.id(),
                        "New ChatHub message",
                        "Open ChatHub to view your message"
                ));

    }
}
