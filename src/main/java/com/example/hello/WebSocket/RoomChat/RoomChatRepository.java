package com.example.hello.WebSocket.RoomChat;

import com.example.hello.Entity.RoomChat;
import com.example.hello.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomChatRepository extends JpaRepository<RoomChat, UUID> {
    @Query("""
            select urc.roomChat
            from UserRoomChat urc
            join urc.user u
            where u.userId in :userIds
            group by urc.roomChat
            having count (distinct u.userId) = :size
            """)
    Optional<RoomChat> findRoomChatByUserIdInAndSize(List<UUID> userIds, Integer size);

    @Query("""
            select distinct rc
            from RoomChat rc
            join rc.userRoomChats urc
            join urc.user u
            where u.userId = :userId
            order by rc.updatedAt desc
            """)
    Page<RoomChat> findRoomChatByUserId(UUID userId, Pageable pageable);

    @Query("""
            select u
            from UserRoomChat urc
            join urc.user u
            where urc.roomChat.roomChatId = :roomChatId
            """)
    List<User> getUsersByRoomChatId(UUID roomChatId);

    Boolean existsByRoomName(String roomName);
    Optional<RoomChat> findByRoomName(String roomName);
}