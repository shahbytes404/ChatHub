package com.shabytes.backend.service;

import com.shabytes.backend.api.dto.RealtimeEvent;
import com.shabytes.backend.domain.ConversationMember;
import com.shabytes.backend.messaging.RedisRealtimePublisher;
import com.shabytes.backend.repository.ConversationMemberRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class TypingService {

    private final MembershipService membershipService;

    private final ConversationMemberRepository conversationMemberRepository;

    private final RedisRealtimePublisher realtimePublisher;

    public TypingService(MembershipService membershipService, ConversationMemberRepository conversationMemberRepository, RedisRealtimePublisher realtimePublisher) {
        this.membershipService = membershipService;
        this.conversationMemberRepository = conversationMemberRepository;
        this.realtimePublisher = realtimePublisher;
    }

    public void update(UUID actorId, UUID conversationId, boolean typing) {
        membershipService.requireMember(conversationId, actorId);

        conversationMemberRepository.findAllByConversationId(conversationId).stream()
                .map(ConversationMember::getUserId)
                .filter(userId -> !userId.equals(actorId))
                .forEach(target -> realtimePublisher.publish(
                        new RealtimeEvent(
                                typing ? "TYPING_STARTED" : "TYPING_STOPPED",
                                target,
                                conversationId,
                                actorId,
                                null,
                                Map.of("expiresInSeconds", 5),
                                Instant.now()
                        )
                ));
    }
}
