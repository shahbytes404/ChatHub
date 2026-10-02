package com.shahbytes.chathub.service;

import com.shahbytes.chathub.api.dto.response.UserResponse;
import com.shahbytes.chathub.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDirectoryService {
    private final UserAccountRepository repository;

    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(
            UUID currentUserId,
            String query
    ) {
        var normalized = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);

        return repository.searchUsers(
                currentUserId,
                normalized,
                PageRequest.of(0, 50)
        );
    }
}
