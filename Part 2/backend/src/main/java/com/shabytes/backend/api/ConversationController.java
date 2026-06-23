package com.shabytes.backend.api;

import com.shabytes.backend.api.dto.AddMemberRequest;
import com.shabytes.backend.api.dto.ConversationResponse;
import com.shabytes.backend.api.dto.CreateConversationRequest;
import com.shabytes.backend.security.CurrentUser;
import com.shabytes.backend.service.ConversationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {
    private final ConversationService conversationService;
    private final CurrentUser currentUser;

    public ConversationController(ConversationService conversationService, CurrentUser currentUser) {
        this.conversationService = conversationService;
        this.currentUser = currentUser;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationResponse created(
            Authentication authentication,
            @Valid @RequestBody CreateConversationRequest request
    ) {
        return conversationService.create(currentUser.id(authentication), request);
    }

    @GetMapping("/{conversationId}")
    public ConversationResponse get(
            Authentication authentication,
            @PathVariable UUID conversationId
    ) {
        return conversationService.get(currentUser.id(authentication), conversationId);
    }

    @GetMapping
    public List<ConversationResponse> list(Authentication authentication) {
        return conversationService.list(currentUser.id(authentication));
    }

    @PostMapping("/{conversationId}/members")
    public ConversationResponse addMember(Authentication authentication,
                                          @PathVariable UUID conversationId,
                                          @Valid @RequestBody AddMemberRequest request) {
        return conversationService.addMember(currentUser.id(authentication), conversationId, request);
    }
}
