package com.shahbytes.chathub.service;

import com.shahbytes.chathub.api.dto.event.RealtimeEvent;
import com.shahbytes.chathub.api.dto.request.ReceiptRequest;
import com.shahbytes.chathub.api.dto.response.ReceiptResponse;
import com.shahbytes.chathub.domain.ConversationMember;
import com.shahbytes.chathub.domain.OutboxEvent;
import com.shahbytes.chathub.domain.type.EventType;
import com.shahbytes.chathub.domain.type.ReceiptType;
import com.shahbytes.chathub.domain.type.ResourceType;
import com.shahbytes.chathub.exception.ForbiddenException;
import com.shahbytes.chathub.exception.NotFoundException;
import com.shahbytes.chathub.repository.ConversationMemberRepository;
import com.shahbytes.chathub.repository.MessageReceiptRepository;
import com.shahbytes.chathub.repository.MessageRepository;
import com.shahbytes.chathub.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final MembershipService membershipService;
    private final MessageRepository messageRepository;
    private final MessageReceiptRepository receiptRepository;
    private final ConversationMemberRepository memberRepository;
    private final MessageReceiptStateService mrStateService;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ReceiptResponse acknowledge(
            UUID userId,
            UUID conversationId,
            UUID messageId,
            ReceiptRequest request
    ) {
        membershipService.requireMember(conversationId, userId);

        var message = messageRepository.
                findByIdAndConversationId(messageId, conversationId)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        var receipts = receiptRepository.findAllByMessageIdForUpdate(messageId);

        var receipt = receipts.stream()
                .filter(item -> item.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Receipt not found for this user"));

        var now = Instant.now();

        switch (request.type()) {
            case DELIVERED -> receipt.markDelivered(now);

            case READ -> {
                receipt.markRead(now);

                var member = memberRepository
                        .findByConversationIdAndUserId(conversationId, userId)
                        .orElseThrow(() -> new NotFoundException("Conversation Member not found"));

                member.markReadThrough(message.getSequenceNumber());

                memberRepository.save(member);
            }
        }

        var currentMemberIds = memberRepository
                .findAllByConversationId(conversationId)
                .stream()
                .map(ConversationMember::getUserId)
                .toList();

        var receiptState = mrStateService.resolveForSender(
                message,
                message.getSenderId(),
                receiptRepository.findAllByMessageId(messageId),
                currentMemberIds
        ).orElseThrow(
                () -> new ForbiddenException("You are not allowed to view the state of message"));

        var response = new ReceiptResponse(
                receipt.getMessageId(),
                receipt.getUserId(),
                receipt.getDeliveredAt(),
                receipt.getReadAt(),
                receiptState
        );

        var eventType = request.type() == ReceiptType.DELIVERED
                ? EventType.MESSAGE_DELIVERED
                : EventType.MESSAGE_READ;

        outboxRepository.save(
                new OutboxEvent(
                        ResourceType.MESSAGE.name(),
                        message.getId(),
                        eventType.name(),
                        toJson(
                                new RealtimeEvent(
                                        eventType,
                                        message.getSenderId(),
                                        conversationId,
                                        userId,
                                        message.getId(),
                                        response,
                                        Instant.now()
                                )
                        )
                )
        );

        return response;
    }


    private String toJson(RealtimeEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Could not serialize receipt event",
                    exception
            );
        }
    }
}
