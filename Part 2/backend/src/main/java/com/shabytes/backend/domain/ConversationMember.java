package com.shabytes.backend.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversation_members")
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

    @Column(name = "muted_until")
    private Instant mutedUntil;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    protected ConversationMember() {
    }

    public ConversationMember(UUID conversationId, UUID userId, MemberRole role) {
        this.id = UUID.randomUUID();
        this.conversationId = conversationId;
        this.userId = userId;
        this.role = role;
        this.lastReadSequence = 0;
    }

    @PrePersist
    void onCreate() {
        joinedAt = Instant.now();
    }


    public void markReadThrough(long sequence) {
        lastReadSequence = Math.max(lastReadSequence, sequence);
    }

    public boolean isMutedAt(Instant now) {
        return mutedUntil != null && mutedUntil.isAfter(now);
    }

    public UUID getId() {
        return id;
    }

    public UUID getConversationId() {
        return conversationId;
    }

    public UUID getUserId() {
        return userId;
    }

    public MemberRole getRole() {
        return role;
    }

    public long getLastReadSequence() {
        return lastReadSequence;
    }

    public Instant getMutedUntil() {
        return mutedUntil;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}
