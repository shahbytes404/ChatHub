package com.shabytes.backend.service;

import com.shabytes.backend.api.dto.RealtimeEvent;
import com.shabytes.backend.api.dto.ReceiptRequest;
import com.shabytes.backend.api.dto.ReceiptResponse;
import com.shabytes.backend.exception.NotFoundException;
import com.shabytes.backend.messaging.RedisRealtimePublisher;
import com.shabytes.backend.repository.ConversationMemberRepository;
import com.shabytes.backend.repository.MessageReceiptRepository;
import com.shabytes.backend.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ReceiptService {

    private final MembershipService membershipService;
    private final MessageRepository messageRepository;
    private final MessageReceiptRepository receiptRepository;
    private final ConversationMemberRepository memberRepository;
    private final RedisRealtimePublisher realtimePublisher;

    public ReceiptService(MembershipService membershipService, MessageRepository messageRepository, MessageReceiptRepository receiptRepository, ConversationMemberRepository memberRepository, RedisRealtimePublisher realtimePublisher) {
        this.membershipService = membershipService;
        this.messageRepository = messageRepository;
        this.receiptRepository = receiptRepository;
        this.memberRepository = memberRepository;
        this.realtimePublisher = realtimePublisher;
    }

    @Transactional
    public ReceiptResponse acknowledge(UUID userId, UUID conversationId, UUID messageId,
                                       ReceiptRequest request) {
        var member = membershipService.requireMember(conversationId, userId);

        var message = messageRepository.findByIdAndConversationId(messageId, conversationId)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        var receipt = receiptRepository.findByMessageIdAndUserId(messageId, userId)
                .orElseThrow(() -> new NotFoundException("Receipt not found for this user"));

        var now = Instant.now();

        switch (request.type()) {
            case DELIVERED -> receipt.markDelivered(now);
            case READ -> {
                receipt.markRead(now);
                member.markReadThrough(message.getSequenceNumber());
                memberRepository.save(member);
            }
        }

        var response = new ReceiptResponse(
                receipt.getMessageId(),
                receipt.getUserId(),
                receipt.getDeliveredAt(),
                receipt.getReadAt()
        );

        realtimePublisher.publish(
                new RealtimeEvent(
                        "MESSAGE_" + request.type().name(),
                        message.getSenderId(),
                        conversationId,
                        userId,
                        messageId,
                        response,
                        now
                )
        );
        return response;
    }
}
