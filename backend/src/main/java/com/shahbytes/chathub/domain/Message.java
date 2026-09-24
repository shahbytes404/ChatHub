package com.shahbytes.chathub.domain;

import com.shahbytes.chathub.domain.type.MessageType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {
    @Id
    private UUID id;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Column(name = "client_message_id", nullable = false, length = 100)
    private String clientMessageId;

    @Column(name = "sequence_number", nullable = false)
    private long sequenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private MessageType messageType;

    @Column(nullable = false, length = 4000)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Message(
            UUID conversationId, UUID senderId, String clientMessageId,
            long sequenceNumber, MessageType messageType, String content
    ) {
        this.id = UUID.randomUUID();
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.clientMessageId = clientMessageId;
        this.sequenceNumber = sequenceNumber;
        this.messageType = messageType;
        this.content = content.strip();
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}
