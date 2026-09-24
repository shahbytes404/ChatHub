package com.shahbytes.chathub.domain;

import com.shahbytes.chathub.domain.type.MemberRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversation_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConversationMember {
    @Id
    private UUID id;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    @Column(name = "last_read_sequence", nullable = false)
    private long lastReadSequence;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    public ConversationMember(UUID conversationId, UUID userId, MemberRole role) {
        this.id = UUID.randomUUID();
        this.conversationId = conversationId;
        this.userId = userId;
        this.role = role;
        this.lastReadSequence = 0;
    }

    @PrePersist
    void onCreate() {
        this.joinedAt = Instant.now();
    }

    public void markReadThrough(long sequence) {
        lastReadSequence = Math.max(lastReadSequence, sequence);
    }
}
