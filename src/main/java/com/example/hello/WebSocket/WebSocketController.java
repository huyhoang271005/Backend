package com.example.hello.WebSocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class WebSocketController {

    @MessageMapping("/send")
    @SendTo("/topic/messages")
    public Message send(Message message){
        message.setTime(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        return message;
    }
}
