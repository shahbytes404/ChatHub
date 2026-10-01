package com.shahbytes.chathub.service;

import com.shahbytes.chathub.api.dto.response.MessageResponse;
import com.shahbytes.chathub.repository.ConversationMemberRepository;
import com.shahbytes.chathub.repository.DeviceRegistrationRepository;
import com.shahbytes.chathub.service.interfaces.PushNotificationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final DeviceRegistrationRepository deviceRepository;
    private final ConversationMemberRepository memberRepository;
    private final PushNotificationProvider pushProvider;

    @Transactional(readOnly = true)
    public void notifyOfflineUser(
            UUID userId,
            MessageResponse message
    ) {
        var member = memberRepository.findByConversationIdAndUserId(
                message.conversationId(),
                userId
        );

        if (member.isEmpty()) {
            return;
        }

        if (member.get().isMutedAt(Instant.now())) {
            return;
        }

        deviceRepository.findAllByUserIdAndNotificationsEnabled(userId)
                .stream()
                .filter(
                        device -> device.getPushToken() != null
                                && !device.getPushToken().isBlank()
                )
                .forEach(device ->
                        pushProvider.send(
                                device,
                                message.conversationId(),
                                message.id(),
                                "New ChatHub message",
                                "Open ChatHub to view your message"
                        ));
    }
}
