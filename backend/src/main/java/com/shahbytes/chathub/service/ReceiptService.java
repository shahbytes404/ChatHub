package com.shahbytes.chathub.service;

import com.shahbytes.chathub.api.dto.ReceiptRequest;
import com.shahbytes.chathub.api.dto.ReceiptResponse;
import com.shahbytes.chathub.exception.ForbiddenException;
import com.shahbytes.chathub.exception.NotFoundException;
import com.shahbytes.chathub.repository.ConversationMemberRepository;
import com.shahbytes.chathub.repository.MessageReceiptRepository;
import com.shahbytes.chathub.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        var receipt = receiptRepository
                .findByMessageIdAndUserId(messageId, userId)
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

        var receiptState = mrStateService.resolveForSender(
                message,
                message.getSenderId(),
                receiptRepository.findAllByMessageId(messageId)
        ).orElseThrow(
                () -> new ForbiddenException("You are not allowed to view the state of message"));

        return new ReceiptResponse(
                receipt.getMessageId(),
                receipt.getUserId(),
                receipt.getDeliveredAt(),
                receipt.getReadAt(),
                receiptState
        );
    }
}
