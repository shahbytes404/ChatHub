package com.shahbytes.chathub.service;

import com.shahbytes.chathub.api.dto.response.MessageSearchResultResponse;
import com.shahbytes.chathub.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageSearchService {
    private final MessageRepository messageRepository;

    @Transactional(readOnly = true)
    public List<MessageSearchResultResponse> search(
            UUID userId, String query, int size
    ) {
        var pageable = PageRequest.of(0,
                Math.min(Math.max(size, 1), 50)
        );

        return messageRepository.searchVisibleMessages(userId, query.strip(), pageable)
                .map(message -> new MessageSearchResultResponse(
                        message.getId(),
                        message.getConversationId(),
                        message.getSenderId(),
                        message.getContent(),
                        message.getCreatedAt(),
                        message.getSequenceNumber()
                )).getContent();
    }
}
