package com.example.hello.Entity;

import com.example.hello.Enum.VerificationTypes;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_token")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerificationTokens {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "verification_id")
    UUID verificationTokenId;
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    Instant createdAt;
    @Column(name = "expired_at")
    Instant expiredAt;

    @Column(name = "type_id")
    UUID typeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    VerificationTypes verificationType;
}
