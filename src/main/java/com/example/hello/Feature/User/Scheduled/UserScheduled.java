package com.example.hello.Feature.User.Scheduled;

import com.example.hello.Enum.UserStatus;
import com.example.hello.Feature.User.Repository.UserRepository;
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
public class UserScheduled {
    UserRepository userRepository;

    @Scheduled(fixedRate = 20*60*1000) //20 min
    @Transactional
    public void deleteUserScheduled() {
        userRepository.deleteByUserStatusAndProfile_CreatedAtBefore(UserStatus.PENDING,
                Instant.now().minus(7, ChronoUnit.DAYS));
        log.info("Scheduled delete users register but not valid email in 7 day");
    }
}
