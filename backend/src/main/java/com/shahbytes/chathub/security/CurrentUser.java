package com.shahbytes.chathub.security;

import com.shahbytes.chathub.exception.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUser {
    public UUID id(Authentication authentication) {
        if (authentication == null ||
                !(authentication.getPrincipal() instanceof ChatPrincipal principal)) {
            throw new ForbiddenException("Authentication is required");
        }
        return principal.userId();
    }
}
