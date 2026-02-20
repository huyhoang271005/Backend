package com.example.hello.Feature.User.Scheduled;

import com.example.hello.Feature.User.Repository.SessionRepository;
import com.example.hello.Infrastructure.Jwt.JwtProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
public class SessionScheduled {
    SessionRepository sessionRepository;
    JwtProperties jwtProperties;

    @Scheduled(fixedRate = 60*60*1000)
    @Transactional
    public void deleteSessionsAndDevice() {
        sessionRepository.deleteSessionExpired(Instant.now().minus(1, ChronoUnit.DAYS));
        log.info("Deleted sessions expired scheduled successfully");
        sessionRepository.deleteDeviceNull();
        log.info("Deleted orphan devices scheduled successfully");
    }

    @Transactional
    @Scheduled(fixedRate = 10*60*1000)
    public void revokeExpiredSessions(){
        var timeAgo = Instant.now().minus(jwtProperties.getRefreshTokenSeconds(), ChronoUnit.SECONDS);
        sessionRepository.revokeExpiredSessions(timeAgo);
        log.info("Revoked scheduled verification expired");
    }

    @Transactional
    @Scheduled(fixedRate = 10*60*1000)
    public void deleteSessionsRevoked(){
        sessionRepository.deleteByRevokedAndLastLoginBefore(true, Instant.now()
                .minus(jwtProperties.getRefreshTokenSeconds() * 2, ChronoUnit.SECONDS));
        log.info("Deleted sessions revoked scheduled successfully");

    }
}
