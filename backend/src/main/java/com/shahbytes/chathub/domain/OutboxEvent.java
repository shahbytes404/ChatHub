package com.shahbytes.chathub.domain;

import com.shahbytes.chathub.domain.type.OutboxStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    @Id
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 40)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false, length = 60)
    private String eventType;

    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxStatus status;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "available_at", nullable = false)
    private Instant availableAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    public OutboxEvent(
            String aggregateType,
            UUID aggregateId,
            String eventType,
            String payloadJson
    ) {
        this.id = UUID.randomUUID();
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payloadJson = payloadJson;
        this.status = OutboxStatus.PENDING;
        this.attempts = 0;
        this.availableAt = Instant.now();
        this.createdAt = Instant.now();
    }

    public void markPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = Instant.now();
    }

    public void scheduleRetry() {
        this.attempts++;

        status = attempts >= 10 ? OutboxStatus.FAILED : OutboxStatus.PENDING;

        /*
        attempt 1 -> 2 seconds
        attempt 2 -> 4 seconds
        attempt 3 -> 8 seconds
        ...
        capped at 300 seconds
         */

        availableAt = Instant.now().plusSeconds(
                Math.min(
                        300,
                        1L << Math.min(attempts, 8)
                )
        );
    }
}
