package com.shabytes.backend.api;

import com.shabytes.backend.api.dto.*;
import com.shabytes.backend.security.CurrentUser;
import com.shabytes.backend.service.MessageQueryService;
import com.shabytes.backend.service.MessageService;
import com.shabytes.backend.service.ReceiptService;
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
    private final MessageQueryService messageQueryService;
    private final ReceiptService receiptService;

    public MessageController(MessageService messageService, CurrentUser currentUser, MessageQueryService messageQueryService, ReceiptService receiptService) {
        this.messageService = messageService;
        this.currentUser = currentUser;
        this.messageQueryService = messageQueryService;
        this.receiptService = receiptService;
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

    @GetMapping
    public MessagePageResponse sync(
            Authentication authentication,
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") long afterSequence,
            @RequestParam(defaultValue = "50") int size
    ) {
        return messageQueryService.getMessages(currentUser.id(authentication), conversationId, afterSequence, size);
    }

    @PostMapping("/{messageId}/receipts")
    public ReceiptResponse receipt(Authentication authentication,
                                   @PathVariable UUID conversationId,
                                   @PathVariable UUID messageId,
                                   @Valid @RequestBody ReceiptRequest request) {
        return receiptService.acknowledge(
                currentUser.id(authentication),
                conversationId,
                messageId,
                request
        );
    }
}
