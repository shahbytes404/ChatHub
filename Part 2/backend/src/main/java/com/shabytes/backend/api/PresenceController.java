package com.shabytes.backend.api;

import com.shabytes.backend.api.dto.PresenceResponse;
import com.shabytes.backend.security.CurrentUser;
import com.shabytes.backend.service.MembershipService;
import com.shabytes.backend.service.PresenceService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/conversations/{conversationId}/presence")
public class PresenceController {
    private final MembershipService membershipService;
    private final PresenceService presenceService;
    private final CurrentUser currentUser;

    public PresenceController(MembershipService membershipService, PresenceService presenceService, CurrentUser currentUser) {
        this.membershipService = membershipService;
        this.presenceService = presenceService;
        this.currentUser = currentUser;
    }

    @GetMapping("/{userId}")
    public PresenceResponse get(Authentication authentication,
                                @PathVariable UUID conversationId,
                                @PathVariable UUID userId
    ) {
        membershipService.requireMember(conversationId, currentUser.id(authentication));
        membershipService.requireMember(conversationId, userId);

        return presenceService.get(userId);
    }
}
