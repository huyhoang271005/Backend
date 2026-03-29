package com.example.hello.Feature.Message.Repository;

import com.example.hello.Entity.Status;
import com.example.hello.Feature.Message.dto.MessageStatus;
import com.example.hello.Feature.RoomChat.dto.CountMessageInfo;
import com.example.hello.Feature.RoomChat.dto.MessageStatusInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StatusRepository extends JpaRepository<Status, UUID> {
    @Query("""
            select m as message, s.messageStatus as messageStatus, s.updatedAt as updatedAt,
                        s.messageStatusId as messageStatusId, m.messageId as messageId
            from Status s
            join s.user u
            join s.message m
            join m.roomChat rc
            where u.userId = :userId and rc.roomChatId = :roomChatId and (m.createdAt > :deletedAt or :deletedAt is null )
            order by m.createdAt desc
            """)
    Page<MessageStatusInfo> getMessageStatusByUserIdAndRoomChatId(UUID userId, UUID roomChatId, Instant deletedAt, Pageable pageable);

    Integer countByUser_UserIdAndMessageStatus(UUID userId, MessageStatus messageStatus);

    @Query("""
            select count(s) as messageCount, rc.roomChatId as roomChatId
            from Status s
            join s.message m
            join m.roomChat rc
            where rc.roomChatId in :roomChatIds and s.messageStatus = :messageStatus
                        and s.user.userId = :userId
            group by rc.roomChatId
            """)
    List<CountMessageInfo> getCountByRoomChatIds( List<UUID> roomChatIds, MessageStatus messageStatus, UUID userId);

    @Modifying
    @Query("""
            update Status s
            set s.messageStatus = :messageStatus
            where s.user.userId = :userId and s.message.roomChat.roomChatId = :roomChatId
            """)
    void readMessage(MessageStatus messageStatus, UUID userId, UUID roomChatId);

    Optional<Status> findFirstByMessage_RoomChat_RoomChatIdAndUser_UserIdOrderByMessage_CreatedAtDesc(UUID RoomChatId, UUID userId);

    @Query("""
            select s
            from Status s
            join s.message m
            join m.roomChat rc
            where m.messageId = :messageId and rc.roomChatId = :roomChatId and s.user.userId = :userId
            """)
    Optional<Status> findByMessageIdAndRoomChatIdAndUserId(UUID messageId, UUID roomChatId,UUID userId);

}