package com.example.hello.Infrastructure.Cache;

import com.example.hello.Middleware.ParamName;
import com.example.hello.Feature.RolePermission.Repository.RolePermissionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RolePermissionCacheService {
    RolePermissionRepository rolePermissionRepository;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ParamName.ROLE_PERMISSIONS_CACHE, key = "#roleId")
    public List<String> getPermissionsCache(UUID roleId) {
        return rolePermissionRepository.findByRole_RoleId(roleId).stream()
                .map(rolePermission -> rolePermission.getPermission().getPermissionName())
                .toList();
    }

    @CacheEvict(cacheNames = ParamName.ROLE_PERMISSIONS_CACHE, key = "#roleId")
    public void invalidatePermissionCache(UUID roleId) {
        log.info("invalidatePermissionCache roleId={}", roleId);
    }
}
