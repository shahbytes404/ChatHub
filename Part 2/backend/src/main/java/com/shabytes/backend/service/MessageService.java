package com.shabytes.backend.service;

import com.shabytes.backend.api.dto.MessageResponse;
import com.shabytes.backend.api.dto.MessageType;
import com.shabytes.backend.api.dto.SendMessageRequest;
import com.shabytes.backend.domain.ConversationMember;
import com.shabytes.backend.domain.Message;
import com.shabytes.backend.domain.MessageReceipt;
import com.shabytes.backend.domain.OutboxEvent;
import com.shabytes.backend.exception.ConflictException;
import com.shabytes.backend.exception.NotFoundException;
import com.shabytes.backend.messaging.MessageCreatedEvent;
import com.shabytes.backend.repository.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

@Service
public class MessageService {
    private final MembershipService membershipService;
    private final MessageRepository messageRepository;
    private final RateLimitService rateLimitService;
    private final ConversationMemberRepository memberRepository;
    private final UserBlockRepository userBlockRepository;
    private final ConversationRepository conversationRepository;
    private final MessageReceiptRepository messageReceiptRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;

    public MessageService(MembershipService membershipService, MessageRepository messageRepository, RateLimitService rateLimitService, ConversationMemberRepository memberRepository, UserBlockRepository userBlockRepository, ConversationRepository conversationRepository, MessageReceiptRepository messageReceiptRepository, OutboxRepository outboxRepository, ObjectMapper objectMapper, AuditService auditService) {
        this.membershipService = membershipService;
        this.messageRepository = messageRepository;
        this.rateLimitService = rateLimitService;
        this.memberRepository = memberRepository;
        this.userBlockRepository = userBlockRepository;
        this.conversationRepository = conversationRepository;
        this.messageReceiptRepository = messageReceiptRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.auditService = auditService;
    }

    @Transactional
    @CacheEvict(cacheNames = "recentMessages", allEntries = true)
    public MessageResponse send(UUID senderId, UUID conversationId, SendMessageRequest request) {
        membershipService.requireMember(conversationId, senderId);

        var existing = messageRepository.
                findBySenderIdAndClientMessageId(senderId, request.clientMessageId());
        if (existing.isPresent()) {
            var message = existing.get();
            if (!message.getConversationId().equals(conversationId)
                    ||
                    !message.getContent().equals(request.content().strip())) {
                throw new ConflictException("clientMessageId was already used for different content");
            }
            return toResponse(message);
        }

        rateLimitService.checkMessageSend(senderId);

        var members = memberRepository.findAllByConversationId(conversationId);
        var recipientIds = members.stream()
                .map(ConversationMember::getUserId)
                .filter(userId -> !userId.equals(senderId))
                .toList();

        var blocked = recipientIds.stream()
                .anyMatch(recipientId -> userBlockRepository
                        .existsByBlockerIdAndBlockedId(recipientId, senderId));

        if (blocked) {
            throw new ConflictException("Message cannot be delivered to one or more recipients");
        }

        var conversation = conversationRepository.
                findByIdForUpdate(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        var message = messageRepository.save(
                new Message(
                        conversationId,
                        senderId,
                        request.clientMessageId(),
                        conversation.allocateNextSequence(),
                        MessageType.TEXT,
                        request.content()
                )
        );

        messageReceiptRepository.saveAll(recipientIds.stream()
                .map(recipientId -> new MessageReceipt(message.getId(), recipientId))
                .toList()
        );

        var response = toResponse(message);

        outboxRepository.save(
                new OutboxEvent(
                        "MESSAGE",
                        message.getId(),
                        "MESSAGE_CREATED",
                        toJson(new MessageCreatedEvent(response, recipientIds))
                )
        );

        auditService.record(
                senderId,
                "MESSAGE_SENT",
                "MESSAGE",
                message.getId().toString(),
                Map.of(
                        "conversationId", conversationId,
                        "sequence", message.getSequenceNumber()
                )
        );

        return response;
    }

    public static MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getConversationId(),
                message.getSenderId(),
                message.getClientMessageId(),
                message.getSequenceNumber(),
                message.getMessageType(),
                message.getContent(),
                message.getCreatedAt()
        );
    }

    private String toJson(MessageCreatedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Could not serialize message event", exception);
        }
    }
}
