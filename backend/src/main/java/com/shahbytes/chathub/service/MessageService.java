package com.shahbytes.chathub.service;

import com.shahbytes.chathub.api.dto.event.MessageCreatedEvent;
import com.shahbytes.chathub.api.dto.response.MessageResponse;
import com.shahbytes.chathub.api.dto.request.SendMessageRequest;
import com.shahbytes.chathub.domain.Message;
import com.shahbytes.chathub.domain.MessageReceipt;
import com.shahbytes.chathub.domain.OutboxEvent;
import com.shahbytes.chathub.domain.type.MessageType;
import com.shahbytes.chathub.domain.type.ReceiptState;
import com.shahbytes.chathub.exception.ConflictException;
import com.shahbytes.chathub.exception.ForbiddenException;
import com.shahbytes.chathub.exception.NotFoundException;
import com.shahbytes.chathub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MembershipService membershipService;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository cmRepository;
    private final MessageReceiptRepository mrRepository;
    private final MessageReceiptStateService receiptStateService;
    private final UserBlockRepository userBlockRepository;

    private final OutboxRepository outboxRepository;
    private final AuditService auditService;

    private final RateLimitService rateLimitService;

    private final ObjectMapper objectMapper;

    @Transactional
    public MessageResponse send(
            UUID senderId,
            UUID conversationId,
            SendMessageRequest request
    ) {
        membershipService.requireMember(conversationId, senderId);

        var existing = messageRepository
                .findBySenderIdAndClientMessageId(senderId, request.clientMessageId());

        if (existing.isPresent()) {
            var message = existing.get();

            // The same clientMessageId must not be reused
            // for different content or a different conversation

            if (
                    !message.getConversationId().equals(conversationId)
                            || !message.getContent().equals(request.content().strip())
            ) {
                throw new ConflictException(
                        "clientMessageId was already used for different content"
                );
            }

            ReceiptState existingState =
                    receiptStateService.resolveForSender(
                            message,
                            senderId,
                            mrRepository.findAllByMessageId(message.getId())
                    ).orElseThrow(
                            () -> new ForbiddenException("You are not allowed to view the state of message"));

            return toResponse(message, existingState);
        }

        rateLimitService.checkMessageSend(senderId);

        var recipientIds = cmRepository.findRecipientIds(conversationId, senderId);

        if (recipientIds.isEmpty()) {
            throw new NotFoundException("No recipients found for the sender");
        }

        var senderBlockedRecipientIds =
                userBlockRepository.findBlockedRecipientIds(
                        senderId, recipientIds
                );

        var recipientsWhoBlockedSender =
                userBlockRepository.findRecipientsWhoBlockedSender(
                        senderId,
                        recipientIds
                );

        var blockedRecipientIds = new HashSet<>(senderBlockedRecipientIds);
        blockedRecipientIds.addAll(recipientsWhoBlockedSender);

        var deliverableRecipientIds = recipientIds.stream()
                .filter(recipientId -> !blockedRecipientIds.contains(recipientId))
                .toList();

        var conversation = conversationRepository.findByIdForUpdate(conversationId)
                .orElseThrow(() ->
                        new NotFoundException("Conversation not found"));

        long sequenceNumber = conversation.allocateNextMessageSequence();

        var message = new Message(
                conversationId,
                senderId,
                request.clientMessageId(),
                sequenceNumber,
                MessageType.TEXT,
                request.content().strip()
        );

        messageRepository.save(message);

        if (!deliverableRecipientIds.isEmpty()) {
            mrRepository.saveAll(
                    deliverableRecipientIds.stream()
                            .map(recipientId ->
                                    new MessageReceipt(
                                            message.getId(),
                                            recipientId
                                    ))
                            .toList()
            );
        }

        var response = toResponse(message, ReceiptState.SENT);

        outboxRepository.save(
                new OutboxEvent(
                        "MESSAGE",
                        message.getId(),
                        "MESSAGE_CREATED",
                        toJson(
                                new MessageCreatedEvent(
                                        response,
                                        deliverableRecipientIds
                                )
                        )
                )
        );

        auditService.record(
                senderId,
                "MESSAGE_SENT",
                "MESSAGE",
                message.getId().toString(),
                Map.of(
                        "conversationId", conversationId,
                        "sequence", sequenceNumber
                )
        );

        return response;
    }

    @Transactional
    public MessageResponse sendSystemMessage(
            UUID actorId,
            UUID conversationId,
            String content,
            List<UUID> recipientIds
    ) {
        var conversation = conversationRepository.findByIdForUpdate(conversationId)
                .orElseThrow(() ->
                        new NotFoundException("Conversation not found"));

        long sequenceNumber = conversation.allocateNextMessageSequence();

        var message = new Message(
                conversationId,
                actorId,
                "system-" + UUID.randomUUID(),
                sequenceNumber,
                MessageType.SYSTEM,
                content
        );
        messageRepository.save(message);

        return toResponse(message, null);
    }

    public static MessageResponse toResponse(Message message, ReceiptState state) {
        return new MessageResponse(
                message.getId(),
                message.getConversationId(),
                message.getSenderId(),
                message.getClientMessageId(),
                message.getSequenceNumber(),
                message.getMessageType(),
                message.getContent(),
                message.getCreatedAt(),
                state
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
