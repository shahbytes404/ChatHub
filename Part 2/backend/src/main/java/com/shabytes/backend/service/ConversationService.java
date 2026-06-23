package com.shabytes.backend.service;

import com.shabytes.backend.api.dto.AddMemberRequest;
import com.shabytes.backend.api.dto.ConversationResponse;
import com.shabytes.backend.api.dto.CreateConversationRequest;
import com.shabytes.backend.api.dto.MemberResponse;
import com.shabytes.backend.domain.Conversation;
import com.shabytes.backend.domain.ConversationMember;
import com.shabytes.backend.domain.ConversationType;
import com.shabytes.backend.domain.MemberRole;
import com.shabytes.backend.exception.ConflictException;
import com.shabytes.backend.exception.NotFoundException;
import com.shabytes.backend.repository.ConversationMemberRepository;
import com.shabytes.backend.repository.ConversationRepository;
import com.shabytes.backend.repository.UserAccountRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConversationService {
    private final UserAccountRepository userAccountRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final AuditService auditService;
    private final MembershipService membershipService;

    public ConversationService(UserAccountRepository userAccountRepository, ConversationRepository conversationRepository, ConversationMemberRepository conversationMemberRepository, AuditService auditService, MembershipService membershipService) {
        this.userAccountRepository = userAccountRepository;
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository;
        this.auditService = auditService;
        this.membershipService = membershipService;
    }

    @Transactional
    public ConversationResponse create(UUID creatorId, @Valid CreateConversationRequest request) {
        var memberIds = new LinkedHashSet<>(request.memberIds());
        memberIds.add(creatorId);

        if (request.type() == ConversationType.DIRECT && memberIds.size() != 2) {
            throw new ConflictException("A direct conversation must contain exactly two members");
        }

        if (request.type() == ConversationType.GROUP && memberIds.size() < 3) {
            throw new ConflictException("A group conversation must contain at least three members");
        }

        var users = userAccountRepository.findAllById(memberIds);
        if (users.size() != memberIds.size()) {
            throw new NotFoundException("One or more conversation members do not exists");
        }

        var conversation = conversationRepository.save(
                new Conversation(
                        request.type(),
                        request.title(),
                        creatorId
                )
        );
        var members = memberIds.stream()
                .map(userId -> new ConversationMember(
                        conversation.getId(),
                        userId,
                        userId.equals(creatorId) ? MemberRole.OWNER : MemberRole.MEMBER
                )).toList();
        conversationMemberRepository.saveAll(members);

        auditService.record(
                creatorId,
                "CONVERSATION_CREATED",
                "CONVERSATION",
                conversation.getId().toString(),
                Map.of(
                        "type", request.type(),
                        "memberCount", members.size()
                )
        );

        return toResponse(conversation);
    }

    private ConversationResponse toResponse(Conversation conversation) {
        var members = conversationMemberRepository.findAllByConversationId(conversation.getId());
        var users = userAccountRepository.findAllById(
                        members.stream()
                                .map(ConversationMember::getUserId).toList()
                ).stream()
                .collect(Collectors.toMap(
                        user -> user.getId(),
                        user -> user.getDisplayName()
                ));
        var memberResponses = members.stream()
                .map(member -> new MemberResponse(
                        member.getUserId(),
                        users.get(member.getUserId()),
                        member.getRole(),
                        member.getLastReadSequence()
                )).toList();
        return new ConversationResponse(
                conversation.getId(),
                conversation.getType(),
                conversation.getTitle(),
                conversation.getCreatedBy(),
                conversation.getCreatedAt(),
                memberResponses
        );
    }

    @Transactional(readOnly = true)
    public ConversationResponse get(UUID id, UUID conversationId) {
        membershipService.requireMember(conversationId, id);
        var conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));
        return toResponse(conversation);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> list(UUID userId) {
        return conversationMemberRepository.
                findAllByUserIdOrderByJoinedAtDesc(userId).stream()
                .map(member -> conversationRepository.findById(member.getConversationId())
                        .orElseThrow(() -> new NotFoundException("Conversation not found"))
                ).map(this::toResponse)
                .toList();
    }

    @Transactional
    public ConversationResponse addMember(UUID actorId, UUID conversationId, AddMemberRequest request) {
        var actor = membershipService.requireManager(conversationId, actorId);
        var conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));
        if (conversation.getType() == ConversationType.DIRECT) {
            throw new ConflictException("Members cannot be added to a direct conversation");
        }

        if (!userAccountRepository.existsById(request.userId())) {
            throw new NotFoundException("User nto found");
        }

        if (conversationMemberRepository.existsByConversationIdAndUserId(conversationId, request.userId())) {
            throw new ConflictException("User is alreadya  member");
        }

        if (request.role() == MemberRole.OWNER && actor.getRole() != MemberRole.OWNER) {
            throw new ConflictException("Only the owner can assign the owner role");
        }

        conversationMemberRepository.save(
                new ConversationMember(conversationId, request.userId(), request.role())
        );

        auditService.record(actorId, "CONVERSATION_MEMBER_ADDED", "CONVERSATION",
                conversationId.toString(),
                Map.of(
                        "userId", request.userId(),
                        "role", request.role()
                ));
        return toResponse(conversation);
    }
}
