package com.example.hello.WebSocket.RoomChat;

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
}
