package com.shabytes.backend.service;

import com.shabytes.backend.domain.ConversationMember;
import com.shabytes.backend.exception.ForbiddenException;
import com.shabytes.backend.repository.ConversationMemberRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MembershipService {
    private final ConversationMemberRepository conversationMemberRepository;

    public MembershipService(ConversationMemberRepository conversationMemberRepository) {
        this.conversationMemberRepository = conversationMemberRepository;
    }

    public ConversationMember requireMember(UUID conversationId, UUID userId) {
        return conversationMemberRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ForbiddenException(
                        "You are not member of this conversation"
                ));
    }

    public ConversationMember requireManager(UUID conversationId, UUID userId) {
        var member = requireMember(conversationId, userId);
        if (!member.getRole().canManagerMembers()) {
            throw new ForbiddenException("This action requires owner or admin role");
        }
        return member;
    }
}
