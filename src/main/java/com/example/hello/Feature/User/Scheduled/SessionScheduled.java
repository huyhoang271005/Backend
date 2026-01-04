package com.example.hello.Feature.User.Scheduled;

import com.example.hello.Repository.SessionRepository;
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

    @Scheduled(fixedRate = 60*60*1000)
    @Transactional
    public void deleteSessionsAndDevice() {
        sessionRepository.deleteSessionExpired(Instant.now().minus(1, ChronoUnit.DAYS));
        log.info("Deleted sessions expired scheduled successfully");
        sessionRepository.deleteDeviceNull();
        log.info("Deleted orphan devices scheduled successfully");
    }
}
