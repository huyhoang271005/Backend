package com.example.hello.Feature.Authentication.Scheduled;

import com.example.hello.Repository.VerificationTokensRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VerificationScheduled {
    VerificationTokensRepository verificationTokensRepository;


    @Transactional
    @Scheduled(fixedRate = 10*60*1000)
    public void deleteVerificationExpired(){
        verificationTokensRepository.deleteByExpiredAtBefore(Instant.now());
        log.info("Deleted scheduled verification expired");
    }
}
