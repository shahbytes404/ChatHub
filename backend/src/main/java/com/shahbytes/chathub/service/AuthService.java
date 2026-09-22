package com.shahbytes.chathub.service;

import com.shahbytes.chathub.api.dto.AuthResponse;
import com.shahbytes.chathub.api.dto.LoginRequest;
import com.shahbytes.chathub.api.dto.RegisterRequest;
import com.shahbytes.chathub.domain.UserAccount;
import com.shahbytes.chathub.exception.ConflictException;
import com.shahbytes.chathub.exception.ForbiddenException;
import com.shahbytes.chathub.repository.UserAccountRepository;
import com.shahbytes.chathub.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserAccountRepository userAccountRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuditService auditService;

    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userAccountRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("An Account already exists for this email");
        }

        var user = userAccountRepository.save(
                new UserAccount(request.displayName(), request.email(),
                        passwordEncoder.encode(request.password()))
        );

        auditService.record(user.getId(), "USER_REGISTERED",
                "USER", user.getId().toString(), Map.of());

        return response(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        var user = userAccountRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ForbiddenException("Invalid email or password"));

        if (!user.isEnabled() ||
                !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ForbiddenException("Invalid email or password");
        }

        return response(user);
    }


    private AuthResponse response(UserAccount user) {
        var token = jwtService.issue(user);
        return new AuthResponse(token.value(), "Bearer", token.expiresAt(),
                user.getId(), user.getDisplayName(), user.getEmail());
    }
}
