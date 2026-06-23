package com.shabytes.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "message_receipts")
public class MessageReceipt {
    @Id
    private UUID id;

    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "read_at")
    private Instant readAt;

    protected MessageReceipt() {
    }

    public MessageReceipt(UUID messageId, UUID userId) {
        this.id = UUID.randomUUID();
        this.messageId = messageId;
        this.userId = userId;
    }

    public void markDelivered(Instant now) {
        if (deliveredAt == null) {
            deliveredAt = now;
        }
    }

    public void markRead(Instant now) {
        markDelivered(now);
        if (readAt == null) {
            readAt = now;
        }
    }

    public UUID getMessageId() {
        return messageId;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public Instant getReadAt() {
        return readAt;
    }
}
