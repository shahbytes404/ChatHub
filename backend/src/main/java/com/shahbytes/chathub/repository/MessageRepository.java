package com.shahbytes.chathub.repository;

import com.shahbytes.chathub.domain.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    Optional<Message> findBySenderIdAndClientMessageId(UUID senderId, String clientMessageId);

    Optional<Message> findByIdAndConversationId(UUID id, UUID conversationId);

    /*
    select 1 from userBlock ub where
        (
            ub.blockerId = :userId
            and ub.blockedId = m.senderId
        )
        or
        (
            ub.blockerId = m.senderId
            and ub.blockedId = :userId
        )
     */

    Optional<Message> findTopByConversationIdOrderBySequenceNumberDesc(UUID conversationId);

    @Query("""
            select m from Message m 
                where m.conversationId = :conversationId
                    and m.sequenceNumber > :afterSequence
                        and(
                            m.senderId = :userId
                            or not exists (
                                select 1 
                                      from UserBlock ub
                                           where
                                            (
                                              ub.blockerId = :userId
                                              and ub.blockedId = m.senderId           
                                            )
                                            or
                                            (
                                              ub.blockerId = m.senderId
                                              and ub.blockedId = :userId           
                                            )
                                )
                            )
                        order by m.sequenceNumber asc
            """)
    Slice<Message> findVisibleMessagesAfterSequence(
            @Param("userId") UUID userId,
            @Param("conversationId") UUID conversationId,
            @Param("afterSequence") long afterSequence,
            Pageable pageable
    );

    @Query("""
            select m from Message m 
                where m.conversationId = :conversationId
                    and m.sequenceNumber > :hiddenAfterSequence
                        and(
                            m.senderId = :userId
                            or not exists (
                                select 1 
                                      from UserBlock ub
                                           where
                                            (
                                              ub.blockerId = :userId
                                              and ub.blockedId = m.senderId           
                                            )
                                            or
                                            (
                                              ub.blockerId = m.senderId
                                              and ub.blockedId = :userId           
                                            )
                                )
                            )
                        order by m.sequenceNumber desc
            """)
    Slice<Message> findLatestVisibleMessages(
            @Param("userId") UUID userId,
            @Param("conversationId") UUID conversationId,
            @Param("hiddenAfterSequence") long hiddenAfterSequence,
            Pageable pageable
    );

    @Query("""
                select m
                    from Message m
                        join ConversationMember cm on cm.conversationId=m.conversationId
                            where cm.userId = :userId
                                and lower(m.content) like lower(concat('%',:query,'%'))
                                            order by m.createdAt desc
            """)
    Slice<Message> searchVisibleMessages(UUID userId, String query, Pageable pageable);

    @Query("""
            select m.conversationId, max(m.sequenceNumber) from Message m
                    where m.conversationId in :conversationIds
                            group by m.conversationId
            """)
    List<Object[]> findLatestSequences(
            @Param("conversationIds") Collection<UUID> conversationIds
    );
}
