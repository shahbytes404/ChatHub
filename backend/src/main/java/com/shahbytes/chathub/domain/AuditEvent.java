package com.shahbytes.chathub.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditEvent {

    @Id
    private UUID id;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(name = "resource_type", nullable = false, length = 40)
    private String resourceType;

    @Column(name = "resource_id", length = 100)
    private String resourceId;

    @Column(name = "metadata_json", nullable = false, columnDefinition = "TEXT")
    private String metadataJson;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    public AuditEvent(
            UUID actorId, String action, String resourceType,
            String resourceId, String metadataJson) {
        this.id = UUID.randomUUID();
        this.actorId = actorId;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.metadataJson = metadataJson;
        this.occurredAt = Instant.now();
    }
}
