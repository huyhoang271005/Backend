package com.example.hello.Users.Authentication.Entity;

import com.example.hello.Users.Authentication.Enum.VerificationTypes;
import com.example.hello.Users.User.Entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
    @org.hibernate.annotations.CreationTimestamp
    LocalDateTime createdAt;
    @Column(name = "expired_at")
    LocalDateTime expiredAt;

    @Column(name = "type_id")
    UUID typeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    VerificationTypes verificationType;
}
