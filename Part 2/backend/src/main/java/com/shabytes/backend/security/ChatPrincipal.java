package com.shabytes.backend.security;

import java.security.Principal;
import java.util.UUID;

public record ChatPrincipal(UUID userId, String email) implements Principal {
    @Override
    public String getName() {
        return userId.toString();
    }
}
