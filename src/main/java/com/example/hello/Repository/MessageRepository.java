package com.example.hello.Repository;

import com.example.hello.DataProjection.MessageListInfo;
import com.example.hello.Entity.Message;
import com.example.hello.WebSocket.Message.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    @Query("""
            select m.content as content,
                          m.createdAt as createdAt,
                          rc.roomChatId as roomChatId,
                          m.senderId as senderId
                   from Message m
                   join m.roomChat rc
                   where rc.roomChatId in :roomChatIds
                     and m.createdAt = (
                         select max(m2.createdAt)
                         from Message m2
                         where m2.roomChat = rc
                     )
                   order by m.createdAt desc
            """)
    List<MessageListInfo> findByRoomChatIds(List<UUID> roomChatIds);
}