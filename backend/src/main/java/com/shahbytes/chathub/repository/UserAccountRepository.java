package com.shahbytes.chathub.repository;

import com.shahbytes.chathub.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    boolean existsByEmailIgnoreCase(String email);

    Optional<UserAccount> findByEmailIgnoreCase(String email);
}
