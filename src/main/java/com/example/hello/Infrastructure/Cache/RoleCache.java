package com.example.hello.Infrastructure.Cache;

import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Middleware.ParamName;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Users.User.Repository.UserRepository;
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
public class RoleCache {
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
        return roleId;
    }
}
