package com.example.hello.Feature.Authentication.Repository;

import com.example.hello.Entity.VerificationTokens;
import com.example.hello.Enum.VerificationTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface VerificationTokensRepository extends JpaRepository<VerificationTokens, UUID> {
    Optional<VerificationTokens> findByVerificationTokenId(UUID verificationTokenId);
    int countByVerificationTypeAndTypeId(VerificationTypes type, UUID typeId);
    List<VerificationTokens> findByVerificationTypeAndTypeIdOrderByCreatedAtDesc(VerificationTypes type, UUID typeId);
    void deleteByUser_UserIdAndTypeId(UUID userId, UUID typeId);
    void deleteByExpiredAtBefore(Instant now);
}
