package com.example.hello.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email", indexes = {
        @Index(name = "idx_email_user_id", columnList = "user_id"),
        @Index(name = "idx_email_email", columnList = "email"),
        @Index(name = "idx_email_created_at", columnList = "created_at")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Email {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "email_id")
    UUID emailId;

    @Column(unique = true, columnDefinition = "VARCHAR(255)")
    String email;

    Boolean validated;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

}
