package com.shabytes.backend.repository;

import com.shabytes.backend.domain.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, UUID> {
    List<ConversationMember> findAllByConversationId(UUID conversationId);

    Optional<ConversationMember> findByConversationIdAndUserId(UUID conversationId, UUID userId);

    List<ConversationMember> findAllByUserIdOrderByJoinedAtDesc(UUID userId);

    boolean existsByConversationIdAndUserId(UUID conversationId, UUID userId);
}
