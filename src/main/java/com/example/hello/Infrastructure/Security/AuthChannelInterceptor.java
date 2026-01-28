package com.example.hello.Infrastructure.Security;

import com.example.hello.Infrastructure.Jwt.JwtComponent;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.nio.file.attribute.UserPrincipal;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthChannelInterceptor implements ChannelInterceptor {
    JwtComponent jwtComponent;

    @Override
    public Message<?> preSend(@NonNull Message<?> message,
                              @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = null;
        accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Kiểm tra nếu là frame CONNECT (lúc bắt đầu kết nối)
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Lấy token từ header (Ví dụ: Authorization: Bearer <token>)
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // Giả sử bạn có hàm validateToken trả về UsernamePasswordAuthenticationToken
                UserPrincipal user = () -> jwtComponent.getUserIdFromToken(token).toString();

                // Quan trọng: Gán user vào accessor
                accessor.setUser(user);
            }
        }
        return message;
    }
}