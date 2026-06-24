package com.shabytes.backend.service;

import com.shabytes.backend.api.dto.MessagePageResponse;
import com.shabytes.backend.repository.MessageRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

@Service
public class MessageQueryService {
    private final MembershipService membershipService;
    private final MessageRepository messageRepository;

    public MessageQueryService(MembershipService membershipService, MessageRepository messageRepository) {
        this.membershipService = membershipService;
        this.messageRepository = messageRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "recentMessages",
            key = "#userId + ':' + #conversationId + ':' + #size",
            condition = "#afterSequence == 0"
    )
    public MessagePageResponse getMessages(UUID userId, UUID conversationId, long afterSequence, int size) {
        membershipService.requireMember(conversationId, userId);

        var pageable = PageRequest.of(0, Math.min(Math.max(size, 1), 100));

        if (afterSequence > 0) {
            var slice = messageRepository.
                    findByConversationIdAndSequenceNumberGreaterThanOrderBySequenceNumberAsc(
                            conversationId,
                            afterSequence,
                            pageable
                    );
            var messages = slice.getContent().stream().map(MessageService::toResponse).toList();

            var next = messages.isEmpty() ? null : messages.get(messages.size() - 1).sequenceNumber();
            return new MessagePageResponse(messages, next, slice.hasNext());
        }

        var slice = messageRepository.
                findByConversationIdOrderBySequenceNumberDesc(conversationId, pageable);

        var messages = new ArrayList<>(slice.getContent().stream().map(MessageService::toResponse).toList());
        Collections.reverse(messages);

        var next = messages.isEmpty() ? null : messages.get(messages.size() - 1).sequenceNumber();
        return new MessagePageResponse(messages, next, slice.hasNext());
    }

}
