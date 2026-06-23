package com.shabytes.backend.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversations")
public class Conversation {
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversationType type;

    @Column(length = 120)
    private String title;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "next_message_sequence", nullable = false)
    private long nextMessageSequence;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Conversation() {
    }

    public Conversation(ConversationType type, String title, UUID createdBy) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.title = title == null || title.isBlank() ? null : title.strip();
        this.createdBy = createdBy;
        this.nextMessageSequence = 1;
    }

    @PrePersist
    void onCreate() {
        var now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public long allocateNextSequence() {
        return nextMessageSequence++;
    }

    public UUID getId() {
        return id;
    }

    public ConversationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public long getNextMessageSequence() {
        return nextMessageSequence;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
