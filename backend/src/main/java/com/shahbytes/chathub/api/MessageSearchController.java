package com.shahbytes.chathub.api;

import com.shahbytes.chathub.api.dto.response.MessageSearchResultResponse;
import com.shahbytes.chathub.security.CurrentUser;
import com.shahbytes.chathub.service.MessageSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageSearchController {

    private final MessageSearchService messageSearchService;
    private final CurrentUser currentUser;

    @GetMapping("/search")
    public List<MessageSearchResultResponse> search(
            Authentication authentication,
            @RequestParam String q,
            @RequestParam(defaultValue = "50") int size
    ) {
        return messageSearchService.search(
                currentUser.id(authentication), q, size
        );
    }
}
