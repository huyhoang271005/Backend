package com.example.hello.Repository;

import com.example.hello.DataProjection.UserRomChatInfo;
import com.example.hello.Entity.User;
import com.example.hello.Entity.UserRoomChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserRoomChatRepository extends JpaRepository<UserRoomChat, UUID> {
    @Query("""
            select rc.roomChatId as roomChatId, u.username as username, u.userId as userId,
                        u.profile.imageUrl as imageUrl, r.roleName as roleName
            from UserRoomChat urc
            join urc.roomChat rc
            join urc.user u
            join u.role r
            where rc.roomChatId in :roomChatIds
            """)
    List<UserRomChatInfo> getUsersRoomChat(List<UUID> roomChatIds);

    Boolean existsByRoomChat_RoomNameAndUser(String roomName, User user);
}