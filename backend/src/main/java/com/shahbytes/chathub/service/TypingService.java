package com.shahbytes.chathub.service;

import com.shahbytes.chathub.api.dto.event.RealtimeEvent;
import com.shahbytes.chathub.domain.ConversationMember;
import com.shahbytes.chathub.domain.UserAccount;
import com.shahbytes.chathub.messaging.RedisRealtimePublisher;
import com.shahbytes.chathub.repository.ConversationMemberRepository;
import com.shahbytes.chathub.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TypingService {

    private final MembershipService membershipService;
    private final ConversationMemberRepository memberRepository;
    private final UserAccountRepository userAccountRepository;
    private final RedisRealtimePublisher publisher;

    public void update(
            UUID actorId,
            UUID conversationId,
            boolean typing
    ) {
        membershipService.requireMember(conversationId, actorId);

        var actorDisplayName = userAccountRepository.findById(actorId)
                .map(UserAccount::getDisplayName)
                .orElse("Someone");

        memberRepository.findAllByConversationId(conversationId)
                .stream()
                .map(ConversationMember::getUserId)
                .filter(userId -> !userId.equals(actorId))
                .forEach(targetUserId -> {
                    var evenType = typing
                            ? "TYPING_STARTED"
                            : "TYPING_STOPPED";

                    publisher.publish(
                            new RealtimeEvent(
                                    evenType,
                                    targetUserId,
                                    conversationId,
                                    actorId,
                                    null,
                                    Map.of(
                                            "actorDisplayName", actorDisplayName,
                                            "expiresInSeconds", 5
                                    ),
                                    Instant.now()
                            )
                    );
                });
    }
}
