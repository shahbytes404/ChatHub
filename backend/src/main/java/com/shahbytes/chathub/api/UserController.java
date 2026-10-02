package com.shahbytes.chathub.api;

import com.shahbytes.chathub.api.dto.response.UserResponse;
import com.shahbytes.chathub.security.CurrentUser;
import com.shahbytes.chathub.service.UserDirectoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserDirectoryService userDirectoryService;
    private final CurrentUser currentUser;

    @GetMapping
    public List<UserResponse> list(
            Authentication authentication,
            @RequestParam(defaultValue = "") String q
    ) {
        return userDirectoryService.searchUsers(
                currentUser.id(authentication),
                q
        );
    }
}
