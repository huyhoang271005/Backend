package com.example.hello.Infrastructure.Cache;

import com.example.hello.Middleware.Response;
import com.example.hello.Middleware.StringApplication;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("cache")
public class CacheController {
    CacheManager cacheManager;


    private Response<Void> clearCacheService(){
        cacheManager.getCacheNames().forEach(name -> {
            Cache cache = cacheManager.getCache(name);
            if(cache != null){
                log.info("cache {} has been cleared", name);
                cache.clear();
            }
        });
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }

    @PreAuthorize("hasAuthority('CLEAR_CACHE')")
    @PostMapping("clear")
    public ResponseEntity<?> clearCache() {
        return ResponseEntity.ok().body(clearCacheService());
    }
}
