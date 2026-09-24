package com.shahbytes.chathub.repository;

import com.shahbytes.chathub.domain.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserBlockRepository extends JpaRepository<UserBlock, UUID> {

    boolean existsByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId);
    
    void deleteByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId);

    @Query("select ub.blockedId from UserBlock ub where ub.blockerId = :blockerId")
    List<UUID> findBlockedUserIdsByBlockerId(@Param("blockerId") UUID blockerId);


    @Query("select ub.blockerId from UserBlock ub where ub.blockedId = :blockedId")
    List<UUID> findBlockerUserIdsByBlockedId(@Param("blockedId") UUID blockedId);
}
