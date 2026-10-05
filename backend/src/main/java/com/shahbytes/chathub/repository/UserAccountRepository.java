package com.shahbytes.chathub.repository;

import com.shahbytes.chathub.api.dto.response.UserResponse;
import com.shahbytes.chathub.domain.UserAccount;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    boolean existsByEmailIgnoreCase(String email);

    Optional<UserAccount> findByEmailIgnoreCase(String email);

    @Query("""
            select new com.shahbytes.chathub.api.dto.response.UserResponse(
                    u.id,
                            u.displayName,
                                    u.email
                    )
                            from UserAccount u
                                    where u.enabled = true
                                      and u.id <> :currentUserId 
                        AND(
                                :query = ''
                                OR lower(u.displayName) like lower(concat('%',:query,'%'))
                                OR lower(u.email) like lower(concat('%',:query,'%'))
                                )  
                        order by lower(u.displayName) 
            """)
    List<UserResponse> searchUsers(
            @Param("currentUserId") UUID currentUserId,
            @Param("query") String query,
            Pageable pageable
    );
}
