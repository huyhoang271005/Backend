package com.example.hello.Feature.User.Service;

import com.example.hello.Infrastructure.Cache.SessionCacheService;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Mapper.SessionMapper;
import com.example.hello.Middleware.ListResponse;
import com.example.hello.Middleware.Response;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Repository.SessionRepository;
import com.example.hello.Feature.User.DTO.SessionResponse;
import com.example.hello.Repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SessionService {
    SessionRepository sessionRepository;
    SessionCacheService sessionCacheService;
    UserRepository userRepository;
    SessionMapper sessionMapper;
    @Transactional(readOnly = true)
    public Response<ListResponse<SessionResponse>> getSessions(UUID userId, UUID mySessionId,
                                                               Pageable pageable){
        var session = sessionRepository.getSessions(userId, pageable);
        var response = session.getContent().stream()
                .map(sessionInfo -> {
                    var sessionResponse = sessionMapper.toSessionResponse(sessionInfo);
                    sessionResponse.setThisSession(sessionInfo.getSessionId().equals(mySessionId));
                    sessionResponse.setAddress(sessionMapper.toAddress(sessionInfo));
                    return sessionResponse;
                })
                .toList();
        log.info("Found sessions successfully");
        return  new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                new ListResponse<>(session.hasNext(), response)
        );
    }

    @Transactional
    public Response<Void> logOutAllSession(UUID userId, UUID sessionId){
        //Tìm user theo userId
        var user = userRepository.findById(userId)
                .orElseThrow(()->
                        new EntityNotFoundException(StringApplication.FIELD.USER + StringApplication.FIELD.NOT_EXIST));
        //Lấy các session của user
        var sessions = user.getSessions();
        //Thu hồi hết các quyền đăng nhập của user
        sessions.stream()
                .filter(session -> !session.getRevoked())
                .forEach(session -> {
            if(!session.getSessionId().equals(sessionId)){
                sessionCacheService.updateRevoked(session.getSessionId(), true);
                log.info("Update cache session successfully");
                session.setRevoked(true);
            }
        });
        log.info("Logout all session successfully");
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }

    @Transactional
    public Response<Void> logOutSession(UUID sessionId){
        //Tìm session theo sessionId
        var session = sessionRepository.findById(sessionId).orElseThrow(
                ()-> new EntityNotFoundException(StringApplication.FIELD.SESSION_LOGIN + StringApplication.FIELD.NOT_EXIST)
        );
        //Thu hồi quyền đăng nhập
        session.setRevoked(true);
        sessionCacheService.updateRevoked(sessionId, true);
        log.info("Logout session cache successfully");
        log.info("Logout session successfully");
        return new  Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }

    @Transactional
    public Response<Void> deleteSession(UUID userId, UUID sessionId){
        var session = sessionRepository.findById(sessionId).orElseThrow(
                () -> new EntityNotFoundException(StringApplication.FIELD.SESSION_LOGIN + StringApplication.FIELD.NOT_EXIST)
        );
        if(!session.getUser().getUserId().equals(userId)){
            log.error("User id client not match user id in db");
            throw new ConflictException(StringApplication.FIELD.REQUEST + StringApplication.FIELD.INVALID);
        }
        sessionRepository.delete(session);
        sessionCacheService.evictRevoked(sessionId);
        log.info("Delete session successfully");
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }
}
