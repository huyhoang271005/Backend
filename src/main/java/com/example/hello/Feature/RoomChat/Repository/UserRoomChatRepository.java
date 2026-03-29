package com.example.hello.Feature.RoomChat.Repository;

import com.example.hello.Feature.RoomChat.dto.UserRomChatInfo;
import com.example.hello.Entity.User;
import com.example.hello.Entity.UserRoomChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRoomChatRepository extends JpaRepository<UserRoomChat, UUID> {
    @Query("""
            select rc.roomChatId as roomChatId, u.profile.fullName as fullName, u.userId as userId,
                        u.profile.imageUrl as imageUrl, r.roleName as roleName
            from UserRoomChat urc
            join urc.roomChat rc
            join urc.user u
            join u.role r
            where rc.roomChatId in :roomChatIds
            """)
    List<UserRomChatInfo> getUsersRoomChat(List<UUID> roomChatIds);

    Boolean existsByRoomChat_RoomNameAndUser(String roomName, User user);

    Optional<UserRoomChat> findByRoomChat_RoomChatIdAndUser_UserId(UUID roomChatId, UUID userId);
}
