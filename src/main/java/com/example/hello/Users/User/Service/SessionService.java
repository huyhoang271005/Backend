package com.example.hello.Users.User.Service;

import com.example.hello.Infrastructure.Cache.SessionCacheService;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Middleware.Response;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Users.Authentication.Repository.SessionRepository;
import com.example.hello.Users.User.Repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SessionService {
    SessionRepository sessionRepository;
    SessionCacheService sessionCacheService;
    UserRepository userRepository;
    @Transactional
    public Response<Void> logOutAllSession(UUID userId){
        //Tìm user theo userId
        var user = userRepository.findById(userId)
                .orElseThrow(()->
                        new EntityNotFoundException(StringApplication.FIELD.USER + StringApplication.FIELD.NOT_EXIST));
        //Lấy các session của user
        var sessions = user.getSessions();
        //Thu hồi hết các quyền đăng nhập của user
        sessions.forEach(session -> {
            sessionCacheService.updateRevoked(session.getSessionId(), true);
            session.setRevoked(true);
        });
        userRepository.save(user);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }

    public Response<Void> logOutSession(UUID sessionId){
        //Tìm session theo sessionId
        var session = sessionRepository.findById(sessionId).orElseThrow(
                ()-> new EntityNotFoundException(StringApplication.FIELD.SESSION_LOGIN + StringApplication.FIELD.NOT_EXIST)
        );
        //Thu hồi quyền đăng nhập
        session.setRevoked(true);
        sessionCacheService.updateRevoked(sessionId, true);
        sessionRepository.save(session);
        return new  Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }
}
