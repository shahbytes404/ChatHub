package com.shahbytes.chathub.service;

import com.shahbytes.chathub.api.dto.*;
import com.shahbytes.chathub.domain.Conversation;
import com.shahbytes.chathub.domain.ConversationMember;
import com.shahbytes.chathub.domain.type.ConversationType;
import com.shahbytes.chathub.domain.type.MemberRole;
import com.shahbytes.chathub.exception.ConflictException;
import com.shahbytes.chathub.exception.NotFoundException;
import com.shahbytes.chathub.repository.ConversationMemberRepository;
import com.shahbytes.chathub.repository.ConversationRepository;
import com.shahbytes.chathub.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final UserAccountRepository userAccountRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository convMemberRepository;

    private final MembershipService membershipService;

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

        convMemberRepository.delete(targetMember);

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

        return toResponse(conversation);
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
