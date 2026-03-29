package com.example.hello.Feature.RoomChat.Repository;

import com.example.hello.Entity.RoomChat;
import com.example.hello.Feature.Message.dto.UserProfileInfo;
import com.example.hello.Feature.RoomChat.dto.RoomChatInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomChatRepository extends JpaRepository<RoomChat, UUID> {
    @Query("""
            select rc
            from RoomChat rc
            where (
                select count(distinct urc.user.userId)
                from UserRoomChat urc
                where urc.roomChat = rc
                and urc.user.userId in :userIds
            ) = :size
            """)
    Optional<RoomChat> findRoomChatByUserIdInAndSize(List<UUID> userIds, Integer size);

    @Query("""
            select rc.roomChatId as roomChatId, rc.roomName as roomChatName, urc.roomChatStatus roomChatStatus,
                        urc.deletedAt as deletedAt
            from RoomChat rc
            join rc.userRoomChats urc
            join urc.user u
            where u.userId = :userId
            order by rc.updatedAt desc
            """)
    Page<RoomChatInfo> findRoomChatByUserId(UUID userId, Pageable pageable);

    @Query("""
            select u as user, u.profile.fullName as fullName
            from UserRoomChat urc
            join urc.user u
            where urc.roomChat.roomChatId = :roomChatId
            """)
    List<UserProfileInfo> getUsersByRoomChatId(UUID roomChatId);
}