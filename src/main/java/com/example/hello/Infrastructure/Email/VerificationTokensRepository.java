package com.example.hello.Infrastructure.Email;

import com.example.hello.Users.Authentication.Entity.VerificationTokens;
import com.example.hello.Users.Authentication.Enum.VerificationTypes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface VerificationTokensRepository extends JpaRepository<VerificationTokens, UUID> {
    Optional<VerificationTokens> findByVerificationTokenId(UUID verificationTokenId);
    int countByVerificationTypeAndTypeId(VerificationTypes type, UUID typeId);
    List<VerificationTokens> findByVerificationTypeAndTypeId(VerificationTypes type, UUID typeId);
    void deleteByUser_UserIdAndVerificationType(UUID userId, VerificationTypes type);
}
