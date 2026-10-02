package com.shahbytes.chathub.service;

import com.shahbytes.chathub.api.dto.response.MessagePageResponse;
import com.shahbytes.chathub.api.dto.response.MessageResponse;
import com.shahbytes.chathub.domain.Message;
import com.shahbytes.chathub.domain.MessageReceipt;
import com.shahbytes.chathub.repository.MessageReceiptRepository;
import com.shahbytes.chathub.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageQueryService {

    private final MembershipService membershipService;
    private final MessageRepository messageRepository;
    private final MessageReceiptRepository messageReceiptRepository;
    private final MessageReceiptStateService messageReceiptStateService;

    @Transactional(readOnly = true)
    public MessagePageResponse getMessages(
            UUID userId,
            UUID conversationId,
            long afterSequence,
            int size
    ) {
        var member = membershipService.requireMember(
                conversationId,
                userId
        );

        int pageSize = Math.min(
                Math.max(size, 1),
                100
        );

        long hiddenAfterSequence =
                member.getHiddenAfterSequence() == null
                        ? 0L
                        : member.getHiddenAfterSequence();

        var pageable = PageRequest.of(0, pageSize);

        if (afterSequence > 0) {
            /*
            afterSequence = 10
            hiddenAfterSequence = 20
             */

            long effectiveAfterSequence = Math.max(
                    afterSequence,
                    hiddenAfterSequence
            );

            var slice = messageRepository.findVisibleMessagesAfterSequence(
                    userId,
                    conversationId,
                    effectiveAfterSequence,
                    pageable
            );

            var messages = toResponses(
                    userId,
                    slice.getContent()
            );

            Long nextAfterSequence = messages.isEmpty()
                    ? null
                    : messages.get(messages.size() - 1).sequenceNumber();

            return new MessagePageResponse(
                    messages,
                    nextAfterSequence,
                    slice.hasNext()
            );
        }

        var slice = messageRepository.findLatestVisibleMessages(
                userId,
                conversationId,
                hiddenAfterSequence,
                pageable
        );

        var messages = new ArrayList<>(toResponses(
                userId,
                slice.getContent()));

        Collections.reverse(messages);

        Long nextAfterSequence = messages.isEmpty()
                ? null
                : messages.get(messages.size() - 1).sequenceNumber();

        return new MessagePageResponse(
                messages,
                nextAfterSequence,
                slice.hasNext()
        );

    }

    private List<MessageResponse> toResponses(
            UUID userId,
            List<Message> messages
    ) {
        if (messages.isEmpty()) {
            return List.of();
        }

        var messageIds = messages.stream()
                .map(Message::getId)
                .toList();

        var receiptsByMessageId =
                messageReceiptRepository.findAllByMessageIdIn(messageIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                MessageReceipt::getMessageId
                        ));

        return messages.stream()
                .map(message -> MessageService.toResponse(
                        message,
                        messageReceiptStateService.resolveForSender(
                                message,
                                userId,
                                receiptsByMessageId.getOrDefault(
                                        message.getId(),
                                        List.of()
                                )
                        ).orElse(null)
                )).toList();
    }
}
