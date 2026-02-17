package com.example.hello.WebSocket.Message;

import com.example.hello.Entity.Status;
import com.example.hello.WebSocket.Message.MessageStatus;
import com.example.hello.WebSocket.RoomChat.CountMessageInfo;
import com.example.hello.WebSocket.RoomChat.MessageStatusInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StatusRepository extends JpaRepository<Status, UUID> {
    @Query("""
            select m as message, s.messageStatus as messageStatus, s.updatedAt as updatedAt,
                        s.messageStatusId as messageStatusId
            from Status s
            join s.user u
            join s.message m
            join m.roomChat rc
            where u.userId = :userId and rc.roomChatId = :roomChatId
            order by m.createdAt desc
            """)
    Page<MessageStatusInfo> getMessageStatusByUserIdAndRoomChatId(UUID userId, UUID roomChatId, Pageable pageable);

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
}