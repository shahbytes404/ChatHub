package com.shabytes.backend.api;

import com.shabytes.backend.api.dto.BlockUserRequest;
import com.shabytes.backend.security.CurrentUser;
import com.shabytes.backend.service.UserSafetyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/safety")
public class UserSafetyController {
    private final UserSafetyService userSafetyService;
    private final CurrentUser currentUser;

    public UserSafetyController(UserSafetyService userSafetyService, CurrentUser currentUser) {
        this.userSafetyService = userSafetyService;
        this.currentUser = currentUser;
    }

    @PostMapping("/blocks")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void block(Authentication authentication,
                      @Valid @RequestBody BlockUserRequest request) {
        userSafetyService.block(currentUser.id(authentication), request.userId());
    }
}
