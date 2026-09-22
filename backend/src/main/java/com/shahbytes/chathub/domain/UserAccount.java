package com.shahbytes.chathub.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserAccount {
    @Id
    private UUID id;

    @Column(name = "display_name", nullable = false, length = 80)
    private String displayName;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, unique = true, length = 100)
    private String passwordHash;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UserAccount(String displayName, String email, String passwordHash) {
        this.id = UUID.randomUUID();
        this.displayName = displayName;
        this.email = email.strip().toLowerCase(Locale.ROOT);
        this.passwordHash = passwordHash;
        this.enabled = true;
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
}
