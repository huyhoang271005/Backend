package com.example.hello.Infrastructure.Cache;

import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Middleware.ParamName;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Users.Authentication.Repository.SessionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SessionCacheService {
    SessionRepository sessionRepository;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ParamName.SESSION_CACHE, key = "#sessionId")
    public Boolean getRevoked(UUID sessionId) {
        var session = sessionRepository.findById(sessionId).orElseThrow(
                ()->new EntityNotFoundException(StringApplication.FIELD.SESSION_LOGIN + StringApplication.FIELD.NOT_EXIST)
        );
        return session.getRevoked();
    }

    @CachePut(cacheNames = ParamName.SESSION_CACHE, key = "#sessionId")
    public Boolean updateRevoked(UUID sessionId, Boolean revoked) {
        return revoked;
    }
}
