package com.shahbytes.chathub.api;

import com.shahbytes.chathub.api.dto.BlockStatusResponse;
import com.shahbytes.chathub.api.dto.BlockUserRequest;
import com.shahbytes.chathub.security.CurrentUser;
import com.shahbytes.chathub.service.UserSafetyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/safety")
@RequiredArgsConstructor
public class UserSafetyController {
    private final UserSafetyService userSafetyService;
    private final CurrentUser currentUser;

    @PostMapping("/blocks")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void block(
            Authentication authentication,
            @Valid @RequestBody BlockUserRequest request
    ) {
        userSafetyService.block(
                currentUser.id(authentication), request.userId()
        );
    }

    @GetMapping("/blocks/{userId}")
    public BlockStatusResponse blockStatus(
            Authentication authentication,
            @PathVariable("userId") UUID userId) {
        return new BlockStatusResponse(
                userSafetyService.isBlocked(currentUser.id(authentication), userId)
        );
    }

    @DeleteMapping("/blocks/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unblock(Authentication authentication,
                        @PathVariable("userId") UUID userId) {
        userSafetyService.unblock(currentUser.id(authentication), userId);
    }
}
