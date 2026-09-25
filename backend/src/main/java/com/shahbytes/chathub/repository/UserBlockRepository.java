package com.shahbytes.chathub.repository;

import com.shahbytes.chathub.domain.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface UserBlockRepository extends JpaRepository<UserBlock, UUID> {

    boolean existsByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId);

    void deleteByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId);

    @Query("""
            select ub.blockedId from UserBlock ub
                    where ub.blockerId = :blockerId
                            and ub.blockedId in :recipientIds
            """)
    List<UUID> findBlockedRecipientIds(
            @Param("blockerId") UUID blockerId,
            @Param("recipientIds") Collection<UUID> recipientIds
    );

    @Query("""
            select ub.blockerId from UserBlock ub
                    where ub.blockedId = :blockedId
                            and ub.blockerId in :recipientIds
            """)
    List<UUID> findRecipientsWhoBlockedSender(
            @Param("blockedId") UUID blockedId,
            @Param("recipientIds") Collection<UUID> recipientIds
    );


}
