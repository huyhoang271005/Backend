package com.example.hello.Infrastructure.Cache;

import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Middleware.ParamName;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Feature.User.Repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleCacheService {
    UserRepository userRepository;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ParamName.ROLE_CACHE, key = "#userId")
    public UUID getRoleCache(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> user.getRole().getRoleId())
                .orElseThrow(()->
                        new EntityNotFoundException(StringApplication.FIELD.USER + StringApplication.FIELD.NOT_EXIST));
    }

    @CachePut(cacheNames = ParamName.ROLE_CACHE, key = "#userId")
    public UUID putRoleCache(UUID userId, UUID roleId) {
        log.info("putRoleCache userId={} roleId={}", userId, roleId);
        return roleId;
    }
}
