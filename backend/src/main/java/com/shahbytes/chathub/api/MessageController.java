package com.shahbytes.chathub.api;

import com.shahbytes.chathub.api.dto.response.MessagePageResponse;
import com.shahbytes.chathub.api.dto.response.MessageResponse;
import com.shahbytes.chathub.api.dto.request.ReceiptRequest;
import com.shahbytes.chathub.api.dto.response.ReceiptResponse;
import com.shahbytes.chathub.api.dto.request.SendMessageRequest;
import com.shahbytes.chathub.security.CurrentUser;
import com.shahbytes.chathub.service.MessageQueryService;
import com.shahbytes.chathub.service.MessageService;
import com.shahbytes.chathub.service.ReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/conversations/{conversationId}/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;
    private final CurrentUser currentUser;
    private final ReceiptService receiptService;
    private final MessageQueryService messageQueryService;

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
        return messageQueryService.getMessages(
                currentUser.id(authentication), conversationId, afterSequence, size
        );
    }

    @PostMapping("/{messageId}/receipts")
    public ReceiptResponse receipt(
            Authentication authentication,
            @PathVariable UUID conversationId,
            @PathVariable UUID messageId,
            @Valid @RequestBody ReceiptRequest request
    ) {
        return receiptService.acknowledge(
                currentUser.id(authentication), conversationId, messageId, request
        );
    }
}
