package com.shabytes.backend.service;

import com.shabytes.backend.domain.UserBlock;
import com.shabytes.backend.exception.ConflictException;
import com.shabytes.backend.exception.NotFoundException;
import com.shabytes.backend.repository.UserAccountRepository;
import com.shabytes.backend.repository.UserBlockRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class UserSafetyService {
    private final UserAccountRepository userAccountRepository;
    private final UserBlockRepository userBlockRepository;
    private final AuditService auditService;

    public UserSafetyService(UserAccountRepository userAccountRepository, UserBlockRepository userBlockRepository, AuditService auditService) {
        this.userAccountRepository = userAccountRepository;
        this.userBlockRepository = userBlockRepository;
        this.auditService = auditService;
    }

    @Transactional
    public void block(UUID blockerId, UUID blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new ConflictException("A user cannot block themselves");
        }

        if (!userAccountRepository.existsById(blockedId)) {
            throw new NotFoundException("User not found");
        }

        if (!userBlockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            userBlockRepository.save(new UserBlock(blockerId, blockedId));
        }

        auditService.record(
                blockerId,
                "USER_BLOCKED",
                "USER",
                blockedId.toString(),
                Map.of()
        );
    }
}
