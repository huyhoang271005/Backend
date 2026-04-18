package com.example.hello.Infrastructure.Security.Filter;

import com.example.hello.Feature.RoomChat.Repository.UserRoomChatRepository;
import com.example.hello.Infrastructure.Security.Jwt.JwtComponent;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthChannelInterceptor implements ChannelInterceptor {
    JwtComponent jwtComponent;
    UserRoomChatRepository userRoomChatRepository;
    AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Message<?> preSend(@NonNull Message<?> message,
                              @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor;
        accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Kiểm tra nếu là frame CONNECT (lúc bắt đầu kết nối)
        if (accessor != null && (StompCommand.SEND.equals(accessor.getCommand())
        || StompCommand.CONNECT.equals(accessor.getCommand()) || StompCommand.SUBSCRIBE.equals(accessor.getCommand()))) {
            // Lấy token từ header (Ví dụ: Authorization: Bearer <token>)
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    var userId = jwtComponent.getUserIdFromToken(token);
                    accessor.setUser(userId::toString);
                    if(StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                        String destination = accessor.getDestination();
                        String pattern = "/topic/room/{roomId}";

                        if (destination != null && pathMatcher.match(pattern, destination)) {
                            Map<String, String> variables = pathMatcher.extractUriTemplateVariables(pattern, destination);
                            UUID roomId = UUID.fromString(variables.get("roomId"));
                            if(!userRoomChatRepository.existsByRoomChat_RoomChatIdAndUser_UserId(roomId, userId)){
                                throw new MessageDeliveryException("User can't join this room chat");
                            }
                        }
                    }
                }
                catch (Exception e) {
                    throw new MessageDeliveryException("AUTH_REQUIRED:REFRESH_TOKEN");
                }
            }
            else {
                throw new MessageDeliveryException("MISSING_TOKEN");
            }
        }
        return message;
    }
}