package com.example.hello.Infrastructure.Cache;

import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Middleware.ParamName;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Users.User.Enum.UserStatus;
import com.example.hello.Users.User.Repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserStatusCacheService {
    UserRepository userRepository;
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ParamName.USER_STATUS_CACHE, key = "#userId")
    public UserStatus getUserStatus(UUID userId) {
        var user = userRepository.findById(userId).orElseThrow(
                ()-> new EntityNotFoundException(StringApplication.FIELD.USER + StringApplication.FIELD.NOT_EXIST)
        );
        return user.getUserStatus();
    }

    @CachePut(cacheNames = ParamName.USER_STATUS_CACHE, key = "#userId")
    public UserStatus updateUserStatus(UUID userId, UserStatus userStatus) {
        return userStatus;
    }
}
