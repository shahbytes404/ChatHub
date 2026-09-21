package com.shahbytes.chathub.repository;

import com.shahbytes.chathub.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
}
