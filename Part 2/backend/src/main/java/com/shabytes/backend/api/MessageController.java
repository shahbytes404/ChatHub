package com.shabytes.backend.api;

import com.shabytes.backend.api.dto.MessageResponse;
import com.shabytes.backend.api.dto.SendMessageRequest;
import com.shabytes.backend.security.CurrentUser;
import com.shabytes.backend.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/conversations/{conversationId}/messages")
public class MessageController {
    private final MessageService messageService;
    private final CurrentUser currentUser;

    public MessageController(MessageService messageService, CurrentUser currentUser) {
        this.messageService = messageService;
        this.currentUser = currentUser;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse send(
            Authentication authentication,
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        return messageService.send(currentUser.id(authentication), conversationId, request);
    }
}
