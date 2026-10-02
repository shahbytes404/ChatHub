package com.shahbytes.chathub.service;

import com.shahbytes.chathub.api.dto.event.RealtimeEvent;
import com.shahbytes.chathub.api.dto.request.AddMemberRequest;
import com.shahbytes.chathub.api.dto.request.CreateConversationRequest;
import com.shahbytes.chathub.api.dto.request.UpdateConversationRequest;
import com.shahbytes.chathub.api.dto.response.ConversationMemberResponse;
import com.shahbytes.chathub.api.dto.response.ConversationResponse;
import com.shahbytes.chathub.api.dto.response.MemberResponse;
import com.shahbytes.chathub.domain.Conversation;
import com.shahbytes.chathub.domain.ConversationMember;
import com.shahbytes.chathub.domain.Message;
import com.shahbytes.chathub.domain.OutboxEvent;
import com.shahbytes.chathub.domain.type.ConversationType;
import com.shahbytes.chathub.domain.type.MemberRole;
import com.shahbytes.chathub.exception.ConflictException;
import com.shahbytes.chathub.exception.NotFoundException;
import com.shahbytes.chathub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final UserAccountRepository userAccountRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository convMemberRepository;
    private final MessageRepository messageRepository;

    private final MembershipService membershipService;
    private final MessageService messageService;

    private final ObjectMapper objectMapper;

    private final OutboxRepository outboxRepository;
    private final AuditService auditService;

    @Transactional
    public ConversationResponse create(UUID creatorId, CreateConversationRequest request) {
        var memberIds = new LinkedHashSet<>(request.memberIds());

        memberIds.add(creatorId);

        if (request.type() == ConversationType.DIRECT && memberIds.size() != 2) {
            throw new ConflictException(
                    "A direct conversation must contain exactly two members"
            );
        }

        if (request.type() == ConversationType.GROUP && memberIds.size() < 3) {
            throw new ConflictException(
                    "A group conversation must contain at least three members"
            );
        }

        if (request.type() == ConversationType.DIRECT) {
            var existingConversationOptional = findDuplicateDirectConversation(memberIds);

            if (existingConversationOptional.isPresent()) {
                return toResponse(existingConversationOptional.get());
            }
        }

        var users = userAccountRepository.findAllById(memberIds);

        if (users.size() != memberIds.size()) {
            throw new NotFoundException("One or more conversation members do not exist");
        }

        var conversation = conversationRepository.save(
                new Conversation(
                        request.type(),
                        request.type() == ConversationType.GROUP ? request.title() : null,
                        creatorId
                )
        );

        var members = memberIds.stream()
                .map(memberId -> new ConversationMember(
                        conversation.getId(),
                        memberId,
                        memberId.equals(creatorId)
                                ? MemberRole.OWNER
                                : MemberRole.MEMBER
                )).toList();

        convMemberRepository.saveAll(members);

        if (request.type() == ConversationType.GROUP) {
            var creatorName = users.stream()
                    .filter(user -> user.getId().equals(creatorId))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Creator not found"))
                    .getDisplayName();

            var recipientIds = members.stream().map(ConversationMember::getUserId).toList();

            messageService.sendSystemMessage(
                    creatorId,
                    conversation.getId(),
                    creatorName + " started the conversation",
                    recipientIds
            );
        }

        return toResponse(conversation);
    }

    @Transactional(readOnly = true)
    public ConversationResponse get(
            UUID userId,
            UUID conversationId
    ) {
        membershipService.requireMember(conversationId, userId);

        var conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        return toResponse(conversation);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> list(UUID userId) {
        var conversations = conversationRepository.findAllForUser(userId);

        if (conversations.isEmpty()) {
            return List.of();
        }

        var conversationsIds = conversations.stream()
                .map(Conversation::getId).toList();

        var members = convMemberRepository.findConversationMemberResponses(conversationsIds);

        var membersByConversation = members.stream()
                .collect(
                        Collectors.groupingBy(
                                ConversationMemberResponse::conversationId
                        )
                );

        return conversations.stream()
                .map(conversation -> {
                    var conversationMembers =
                            membersByConversation.getOrDefault(
                                    conversation.getId(),
                                    List.of()
                            );

                    var memberResponses =
                            conversationMembers.stream()
                                    .map(member -> new MemberResponse(
                                            member.userId(),
                                            member.displayName(),
                                            member.role(),
                                            member.lastReadSequence()
                                    )).toList();

                    return new ConversationResponse(
                            conversation.getId(),
                            conversation.getType(),
                            conversation.getTitle(),
                            conversation.getCreatedBy(),
                            conversation.getCreatedAt(),
                            memberResponses
                    );
                }).toList();

    }

    private Optional<Conversation> findDuplicateDirectConversation(
            LinkedHashSet<UUID> memberIds
    ) {
        return conversationRepository.findExistingDirectConversation(ConversationType.DIRECT, memberIds);
    }

    @Transactional
    public ConversationResponse addMember(
            UUID actorId,
            UUID conversationId,
            AddMemberRequest request
    ) {
        membershipService.requireManager(conversationId, actorId);

        var conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        if (conversation.getType().equals(ConversationType.DIRECT)) {
            throw new ConflictException(
                    "Create a group conversation instead of adding to a direct conversation"
            );
        }

        if (!userAccountRepository.existsById(request.userId())) {
            throw new NotFoundException("User not found");
        }

        if (convMemberRepository.existsByConversationIdAndUserId(conversationId, request.userId())) {
            throw new ConflictException("User is already a member");
        }

        var newMember = new ConversationMember(conversationId, request.userId(), request.role());

        convMemberRepository.save(newMember);

        var joinedUser = userAccountRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        var recipientIds = convMemberRepository.
                findAllByConversationId(conversationId).stream()
                .map(ConversationMember::getUserId).toList();

        messageService.sendSystemMessage(
                actorId,
                conversationId,
                joinedUser.getDisplayName() + " joined",
                recipientIds
        );

        return toResponse(conversation);
    }

    @Transactional
    public ConversationResponse removeMember(
            UUID actorId,
            UUID conversationId,
            UUID memberUserId
    ) {
        membershipService.requireManager(conversationId, actorId);

        var conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        if (conversation.getType().equals(ConversationType.DIRECT)) {
            throw new ConflictException(
                    "Direct conversation do not support member removal"
            );
        }

        if (memberUserId.equals(actorId)) {
            throw new ConflictException(
                    "Use delete conversation to leave for yourself"
            );
        }

        var targetMember =
                convMemberRepository.findByConversationIdAndUserId(conversationId, memberUserId)
                        .orElseThrow(() -> new NotFoundException("Member not found"));

        if (targetMember.getRole() == MemberRole.OWNER) {
            throw new ConflictException(
                    "Owner cannot be removed from group"
            );
        }

        var actor = userAccountRepository.findById(actorId)
                .orElseThrow(() -> new NotFoundException("Actor not found"));

        var removedUser = userAccountRepository.findById(memberUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        convMemberRepository.delete(targetMember);

        var recipientIds = convMemberRepository.findAllByConversationId(conversationId).stream()
                .map(ConversationMember::getUserId).toList();

        messageService.sendSystemMessage(
                actorId,
                conversationId,
                actor.getDisplayName() + " removed " + removedUser.getDisplayName(),
                recipientIds
        );

        return toResponse(conversation);
    }

    @Transactional
    public ConversationResponse update(
            UUID actorId,
            UUID conversationId,
            UpdateConversationRequest request
    ) {
        membershipService.requireManager(conversationId, actorId);

        var conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        if (conversation.getType().equals(ConversationType.DIRECT)) {
            throw new ConflictException(
                    "Only group conversations can be updated"
            );
        }

        conversation.rename(request.title());

        var actor = userAccountRepository.findById(actorId)
                .orElseThrow(() -> new NotFoundException("Actor not found"));

        var recipientIds = convMemberRepository.findAllByConversationId(conversationId).stream()
                .map(ConversationMember::getUserId).toList();

        messageService.sendSystemMessage(
                actorId,
                conversationId,
                actor.getDisplayName()
                        + " changed the group name to "
                        + conversation.getTitle(),
                recipientIds
        );

        return toResponse(conversation);
    }

    @Transactional
    public void deleteForUser(
            UUID actorId,
            UUID conversationId,
            boolean permanent
    ) {
        var actor = membershipService.requireMember(conversationId, actorId);

        var conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        if (permanent
                && (
                conversation.getType() != ConversationType.GROUP
                        || actor.getRole() != MemberRole.OWNER
        )) {
            throw new ConflictException(
                    "Permanent delete is only available to the group owner"
            );
        }

        if (permanent) {
            var members = convMemberRepository.findAllByConversationId(conversationId);

            for (var member : members) {
                outboxRepository.save(
                        createConversationRemovedEvent(
                                member.getUserId(),
                                conversationId,
                                actorId,
                                true
                        )
                );
            }

            conversationRepository.deleteById(conversationId);

            auditService.record(
                    actorId,
                    "CONVERSATION_DELETED",
                    "CONVERSATION",
                    conversationId.toString(),
                    Map.of(
                            "scope", "GROUP_HARD_DELETE"
                    )
            );

            return;
        }

        var latestSequence =
                messageRepository
                        .findTopByConversationIdOrderBySequenceNumberDesc(conversationId)
                        .map(Message::getSequenceNumber)
                        .orElse(0L);

        actor.softHideAt(latestSequence);

        convMemberRepository.save(actor);

        outboxRepository.save(
                createConversationRemovedEvent(
                        actorId,
                        conversationId,
                        actorId,
                        false
                )
        );

        auditService.record(
                actorId,
                "CONVERSATION_DELETED_FOR_USER",
                "CONVERSATION",
                conversationId.toString(),
                Map.of()
        );
    }

    private OutboxEvent createConversationRemovedEvent(
            UUID targetUserId,
            UUID conversationId,
            UUID actorId,
            boolean permanent
    ) {
        var event = new RealtimeEvent(
                "CONVERSATION_REMOVED",
                targetUserId,
                conversationId,
                actorId,
                null,
                Map.of(
                        "conversationId", conversationId,
                        "permanent", permanent
                ),
                Instant.now()
        );

        return new OutboxEvent(
                "CONVERSATION",
                conversationId,
                "CONVERSATION_REMOVED",
                toJson(event)
        );
    }

    private String toJson(RealtimeEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Could not serialize conversation event", exception
            );
        }
    }

    private ConversationResponse toResponse(Conversation conversation) {
        var memberResponses = convMemberRepository.findMemberResponses(conversation.getId());

        return new ConversationResponse(
                conversation.getId(),
                conversation.getType(),
                conversation.getTitle(),
                conversation.getCreatedBy(),
                conversation.getCreatedAt(),
                memberResponses
        );
    }
}
