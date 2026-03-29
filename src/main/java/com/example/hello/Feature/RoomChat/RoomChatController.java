package com.example.hello.Feature.RoomChat;

import com.example.hello.Feature.RoomChat.dto.RoomChatDTO;
import com.example.hello.Feature.RoomChat.dto.RoomChatStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("room-chat")
public class RoomChatController {
    RoomChatService roomChatService;

    @PreAuthorize("hasAuthority('ADD_ROOM_CHAT')")
    @PostMapping
    public ResponseEntity<?> addRoomChat(@AuthenticationPrincipal UUID userId,
                                         @RequestBody RoomChatDTO roomChatDTO) {
        return ResponseEntity.ok().body(roomChatService.addRoomChat(userId, roomChatDTO));
    }

    @DeleteMapping("/{roomChatId}")
    public ResponseEntity<?> deleteRoomChat(@AuthenticationPrincipal UUID userId,
                                            @PathVariable UUID roomChatId) {
        return ResponseEntity.ok().body(roomChatService.deleteRoomChat(userId, roomChatId));
    }

    @PatchMapping("/{roomChatId}")
    public ResponseEntity<?> updateRoomChat(@AuthenticationPrincipal UUID userId,
                                            @PathVariable UUID roomChatId,
                                            @RequestBody RoomChatStatus roomChatStatus){
        return ResponseEntity.ok(roomChatService.changeStatusRoomChat(userId, roomChatId, roomChatStatus));
    }
    @GetMapping
    public ResponseEntity<?> getRoomChats(@AuthenticationPrincipal UUID userId,
                                          Pageable pageable) {
        return ResponseEntity.ok().body(roomChatService.getRoomChats(userId, pageable));
    }

    @GetMapping("{roomChatId}/members")
    public ResponseEntity<?> getUsersRoomChat(@AuthenticationPrincipal UUID userId,
                                              @PathVariable UUID roomChatId) {
        return ResponseEntity.ok(roomChatService.getUsers(userId, roomChatId));
    }

    @GetMapping("{roomChatId}/messages")
    public ResponseEntity<?> getMessagesRoomChat(@AuthenticationPrincipal UUID userId,
                                                 @PathVariable UUID roomChatId,
                                                 Pageable pageable) {
        return ResponseEntity.ok(roomChatService.getMessages(userId, roomChatId, pageable));
    }

    @PatchMapping("{roomChatId}/unread")
    public ResponseEntity<?> unreadRoomChat(@AuthenticationPrincipal UUID userId,
                                            @PathVariable UUID roomChatId) {
        return ResponseEntity.ok(roomChatService.unreadMessage(userId, roomChatId));
    }

    @DeleteMapping("{roomChatId}/messages/{messageId}")
    public ResponseEntity<?> deleteMessage(@AuthenticationPrincipal UUID userId,
                                           @PathVariable UUID roomChatId,
                                           @PathVariable UUID messageId) {
        return ResponseEntity.ok(roomChatService.deleteMessage(userId, roomChatId, messageId));
    }
}
