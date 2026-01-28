package com.example.hello.WebSocket.Message;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebSocketController {
    SimpMessagingTemplate template;
    MessageService messageService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload MessageDTO messageDTO,
                            Principal principal){
        var userId = UUID.fromString(principal.getName());
        messageDTO = messageService.saveMessage(userId, messageDTO);
        template.convertAndSend("/topic/room/" + messageDTO.getRoomId(), messageDTO);
    }

    @MessageMapping("/chat.read")
    public void readMessage(@Payload MessageDTO messageDTO,
                            Principal principal){
        var userId = UUID.fromString(principal.getName());
        messageService.readMessage(userId, messageDTO.getRoomId());
    }
}
