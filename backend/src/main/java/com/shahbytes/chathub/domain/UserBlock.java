package com.shahbytes.chathub.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_blocks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBlock {
    @Id
    private UUID id;

    @Column(name = "blocker_id", nullable = false)
    private UUID blockerId;

    @Column(name = "blocked_id", nullable = false)
    private UUID blockedId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public UserBlock(UUID blockerId, UUID blockedId) {
        this.id = UUID.randomUUID();
        this.blockerId = blockerId;
        this.blockedId = blockedId;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}
